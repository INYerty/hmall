package com.inyert.consultant.controller.aiservice;

import com.inyert.consultant.tool.RecommendationTool;
import com.inyert.consultant.tool.ReservationTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ConsultantService {

    private final ChatClient chatClient;

    public ConsultantService(ChatClient.Builder builder, 
                             RecommendationTool recommendationTool,
                             ReservationTool reservationTool,
                             @Value("classpath:system.txt") Resource systemMessage) {
        this.chatClient = builder.build();
        System.out.println("[AI-DEBUG] ChatClient 初始化完成，工具已注册。");
    }

    public Flux<String> chat(String memoryId, String message) {
        System.out.println("[AI-DEBUG] 收到用户消息: " + message);
        
        String response = chatClient.prompt()
                .system(s -> s.text("""
                        你是 INYert 电商平台的智能推荐助手。当用户询问商品时，你必须调用 recommendItems 或 getHotItems 工具获取真实数据，严禁自己编造商品信息。
                        
                        重要：当用户提到价格限制时（如"XX元以下"、"预算在XX以内"等），调用 recommendItems 工具时必须设置 maxPrice 参数为具体金额（单位：元）。例如用户说"700元以下的手机"，则 maxPrice 必须设为 700。
                        """))
                .user(message)
                .advisors(a -> a.param("conversation_id", memoryId))
                .functions("recommendItems", "getHotItems", "insertReservation", "queryReservation")
                .call()
                .content();
        
        System.out.println("[AI-DEBUG] 模型返回结果: " + response);
        return Flux.just(response);
    }
}
