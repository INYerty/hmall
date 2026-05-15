package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClearCartListener {
    private final ICartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.clear.queue", durable = "true"),
            exchange = @Exchange(name = "trade.topic", type = ExchangeTypes.TOPIC),
            key = "order.create"
    ))
    public void listenClearCart(
            Set<Long> itemIds,
            @Header("userId") Long userId  // ✅ 从消息头取 userId
    ) {
        log.info("清空购物车，itemIds:{}，userId:{}", itemIds, userId);

        if (userId == null || itemIds == null || itemIds.isEmpty()) {
            log.error("参数为空，无法清空购物车");
            return;
        }

        // ✅ 注入 UserContext
        UserContext.setUser(userId);
        try {
            cartService.removeByItemIds(itemIds);
        } catch (Exception e) {
            log.error("清空购物车失败", e);
        } finally {
            UserContext.removeUser(); // ✅ 防止 ThreadLocal 泄漏
        }
    }



}

//TODO：请求头方式
// 3.清理购物车商品
/*
String exchangeName = "trade.topic";
try {
        rabbitTemplate.convertAndSend(exchangeName, "order.create", itemIds, message -> {
        // ✅ 将 userId 放入消息头
        message.getMessageProperties().setHeader("userId", UserContext.getUser());
        return message;
    });
            } catch (Exception e) {
        log.error("消息发送失败，订单id：{}", order.getId(), e);
        }


@RabbitListener(bindings = @QueueBinding(
        value = @Queue(name = "cart.clear.queue", durable = "true"),
        exchange = @Exchange(name = "trade.topic", type = ExchangeTypes.TOPIC),
        key = "order.create"
))
public void listenClearCart(
        Set<Long> itemIds,
        @Header("userId") Long userId  // ✅ 从消息头取 userId
) {
    log.info("清空购物车，itemIds:{}，userId:{}", itemIds, userId);

    if (userId == null || itemIds == null || itemIds.isEmpty()) {
        log.error("参数为空，无法清空购物车");
        return;
    }

    // ✅ 注入 UserContext
    UserContext.setUser(userId);
    try {
        cartService.removeByItemIds(itemIds);
    } catch (Exception e) {
        log.error("清空购物车失败", e);
    } finally {
        UserContext.removeUser(); // ✅ 防止 ThreadLocal 泄漏
    }
}*/
