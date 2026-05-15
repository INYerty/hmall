package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {

    private final IOrderService orderService;
    private final PayClient payClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME,durable = "true"),
            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME,delayed = "true"),
            key = MQConstants.DELAY_ORDER_KEY
    ))
    public void listenOrderDelayMessage(Long orderId){
        // 1.查询订单状态 是否为已经支付，如果是未支付，继续执行后续操作，如果是已支付，直接返回
//        throw new RuntimeException("测试 error.queue 消息");
        Order order = orderService.getById(orderId);
        if(order == null || order.getStatus() != 1){
            //2.修改订单状态为已支付
            orderService.markOrderPaySuccess(orderId);
        }else{
            // 3.未支付，需要查询支付流水状态
            PayOrderDTO payOrderDTO = payClient.queryPayOrderByBizOrderNo(orderId);
            // 4.判断是否支付
            if(payOrderDTO != null && payOrderDTO.getStatus() == 3){//已支付
                // 4.1.已支付，标记订单状态为已支付
                orderService.markOrderPaySuccess(orderId);
            }else{
                // TODO 4.2.未支付，取消订单，回复库存 （已完成）
                orderService.cancelOrder(orderId);
            }
        }

        System.out.println("监听到订单延迟消息，订单id："+orderId);
    }
}
