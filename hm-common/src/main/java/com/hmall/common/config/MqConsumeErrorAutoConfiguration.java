package com.hmall.common.config;

/*- 定义一个自动配置类MqConsumeErrorAutoConfiguration，内容包括：
        - 声明一个交换机，名为error.direct，类型为direct
  - 声明一个队列，名为：微服务名 + error.queue，也就是说要动态获取
  - 将队列与交换机绑定，绑定时的RoutingKey就是微服务名
  - 声明RepublishMessageRecoverer，消费失败消息投递到上述交换机
  - 给配置类添加条件，当spring.rabbitmq.listener.simple.retry.enabled为true时触发*/

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(
        name = "spring.rabbitmq.listener.simple.retry.enabled",
        havingValue = "true"
)
public class MqConsumeErrorAutoConfiguration {
    private final RabbitTemplate rabbitTemplate;
    //TODO: 获取服务名
    @Value("${spring.application.name}")
    private String serviceName;
    @Bean
    public DirectExchange errorExchange(){
        return new DirectExchange("error.direct");
    }
    @Bean
    public Queue errorQueue(){
        return QueueBuilder.durable(serviceName + ".error.queue").build();
    }
    @Bean
    public Binding errorBinding(DirectExchange errorExchange, Queue errorQueue){
        return BindingBuilder.bind(errorQueue).to(errorExchange).with(serviceName);
    }

    @Bean
    public MessageRecoverer RepublishMessageRecoverer(){
        return new RepublishMessageRecoverer(rabbitTemplate,errorExchange().getName(),serviceName);
    }
}
