package com.hmall.trade.listener;


import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayStatusListener {
    private final IOrderService orderService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue",durable = "true"),
            exchange = @Exchange(name = "pay.direct" ,type = ExchangeTypes.DIRECT),
            key = "pay.success"
    ))
    public void listenPaySuccess(Long bizOrderNo){
        //查询订单
        Order order = orderService.getById(bizOrderNo);
        Integer status = order.getStatus(); //1 未支付
        //判断订单状态是否为未支付
        //方法1
        /*if(status == 1){
            orderService.markOrderPaySuccess(bizOrderNo);
            log.info("支付成功，订单编号：{}",bizOrderNo);
        }else{
            log.info("订单已支付，订单编号：{}",bizOrderNo);
        }*/
        //方法2
        //第一次来的消息,将订单由未支付修改为已支付,那后续再来的消息order存在+状态!=1,不需要做操作,直接返回
        if(order == null || status != 1){
            return;
        }
        orderService.markOrderPaySuccess(bizOrderNo);
        log.info("支付成功，订单编号：{}",bizOrderNo);
    }
}
