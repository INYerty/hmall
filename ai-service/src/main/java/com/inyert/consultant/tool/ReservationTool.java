package com.inyert.consultant.tool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 志愿预约工具 (Spring AI)
 */
@Component
public class ReservationTool {

    @Bean
    @Description("添加志愿指导服务信息")
    public Function<ReservationRequest, Void> insertReservation() {
        return req -> {
            System.out.println("添加工具被调用: " + req.name());
            return null;
        };
    }

    @Bean
    @Description("查询志愿指导服务信息")
    public Function<QueryRequest, String> queryReservation() {
        return req -> {
            return "暂无预约信息";
        };
    }

    public record ReservationRequest(
            @com.fasterxml.jackson.annotation.JsonProperty(required = true) @com.fasterxml.jackson.annotation.JsonPropertyDescription("姓名") String name,
            @com.fasterxml.jackson.annotation.JsonProperty(required = true) @com.fasterxml.jackson.annotation.JsonPropertyDescription("手机号") String phone,
            @com.fasterxml.jackson.annotation.JsonProperty(required = true) @com.fasterxml.jackson.annotation.JsonPropertyDescription("意向沟通时间") String communicationTime,
            @com.fasterxml.jackson.annotation.JsonProperty(required = false) @com.fasterxml.jackson.annotation.JsonPropertyDescription("邮箱") String email,
            @com.fasterxml.jackson.annotation.JsonProperty(required = false) @com.fasterxml.jackson.annotation.JsonPropertyDescription("购物类别") String category,
            @com.fasterxml.jackson.annotation.JsonProperty(required = false) @com.fasterxml.jackson.annotation.JsonPropertyDescription("备注信息") String notes) {}

    public record QueryRequest(
            @com.fasterxml.jackson.annotation.JsonProperty(required = true) @com.fasterxml.jackson.annotation.JsonPropertyDescription("手机号") String phone) {}
}
