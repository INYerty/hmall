package com.hmall.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.client.CartClient;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.UserContext;


import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.dto.OrderFormDTO;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.domain.po.OrderDetail;
import com.hmall.trade.mapper.OrderMapper;
import com.hmall.trade.service.IOrderDetailService;
import com.hmall.trade.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.ec.ECElGamalDecryptor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

//    private final IItemService itemService;
    private final ItemClient itemService;
    private final IOrderDetailService detailService;
//    private final ICartService cartService;
//    private final CartClient cartClient;
    private final RabbitTemplate rabbitTemplate;
    @Override
    @GlobalTransactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        // 1.订单数据
        Order order = new Order();
        // 1.1.查询商品
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        // 1.2.获取商品id和数量的Map
        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        // 1.3.查询商品
        List<ItemDTO> items = itemService.queryItemByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }
        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(/*UserContext.getUser()*/1L);
        order.setStatus(1);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        // 3.清理购物车商品
//        cartClient.removeByItemIds(itemIds);
        //TODO:改造： 将feign改造成rabbitmq方式实现消息的收发
        String exchangeName = "trade.topic";
//        Map<String,Object> map = new HashMap<>(2);
//        map.put("itemIds",itemIds);
//        map.put("userId",UserContext.getUser());
//        try {
//            rabbitTemplate.convertAndSend(exchangeName,"order.create", map);
//        } catch (Exception e) {
//            log.error("消息发送失败，订单id：{}", order.getId(), e);
//        }
        //TODO：请求头方式
        try {
            rabbitTemplate.convertAndSend(exchangeName, "order.create", itemIds, message -> {
                // ✅ 将 userId 放入消息头
                message.getMessageProperties().setHeader("userId", UserContext.getUser());
                return message;
            });
        } catch (Exception e) {
            log.error("消息发送失败，订单id：{}", order.getId(), e);
        }

        // 4.扣减库存
        try {
            itemService.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！");
        }

        //5.发送延迟消息，订单超时未支付，取消订单，反之则不做任何操作
        rabbitTemplate.convertAndSend(
                MQConstants.DELAY_EXCHANGE_NAME,
                MQConstants.DELAY_ORDER_KEY,
                order.getId(),
                message -> {
                    message.getMessageProperties().setDelay(10000);//TODO：用 15 分钟  (已完成测试  正常)
                    log.info("发送延迟消息，订单id：{}", order.getId());
                    return message;
                }
        );



        return order.getId();
    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
        //TODO 4.1.取消订单，恢复库存
        //1.更新订单状态,订单关闭
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(5); //交易取消，订单关闭
        order.setPayTime(LocalDateTime.now());
        updateById(order);
        //恢复库存 注意一个order可能存在多个商品，应当设置成批量恢复
        List<OrderDetailDTO> detailDTOS = queryItemByOrderId(orderId);
        itemService.restoreStock(detailDTOS);
    }

    private List<OrderDetailDTO> queryItemByOrderId(Long orderId) {
        List<OrderDetailDTO> detailDTOS = new ArrayList<>();
        List<OrderDetail> details = detailService.list(new QueryWrapper<OrderDetail>().eq("order_id", orderId));
        for (OrderDetail detail : details) {
            OrderDetailDTO detailDTO = new OrderDetailDTO();
            detailDTO.setItemId(detail.getItemId());
            detailDTO.setNum(detail.getNum());
            detailDTOS.add(detailDTO);
        }
        return detailDTOS;
    }

    /**
     * 构建订单详情数据
     * @param orderId
     * @param items
     * @param numMap
     * @return
     */
    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }

}
