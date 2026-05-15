package com.hmall.pay;

import com.hmall.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.hmall.pay.mapper")
//自定义扫描的基础包，避免因为扫描不到hm-common的utils中RabbitMqHelper类而报错
//spring扫描的规则是：扫描当前包及其子包下的类，如果是在不同微服务之间调用，则需要指定扫描的包，否则会报错
@SpringBootApplication(scanBasePackages = "com.hmall")
@EnableFeignClients(basePackages = "com.hmall.api.client", defaultConfiguration = DefaultFeignConfig.class)
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }
}