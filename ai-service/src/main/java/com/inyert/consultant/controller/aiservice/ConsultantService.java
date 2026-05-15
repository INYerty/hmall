package com.inyert.consultant.controller.aiservice;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

//@AiService(
//        wiringMode = AiServiceWiringMode.EXPLICIT, //手动装配
//        chatModel = "openAiChatModel" //指定模型
//)
//@AiService

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        //chatMemory = "chatMemory"
        chatMemoryProvider = "chatMemoryProvider", //配置会话记忆提供者对象
        contentRetriever = "contentRetriever",   //配置向量数据库检索对象
        tools = "reservationTool" //配置工具对象
)
public interface ConsultantService {
    @SystemMessage(fromResource = "system.txt")
//    @UserMessage("这是用户的信息{{msg}},你要回答这个问题，并在最后对这个问题举一反三")

    //用于聊天的方法
    //注意：如果添加了@MemoryId注解，chat方法就有了两个参数，一个是MemoryId，
    // 一个就是用户输入的message，这里要对用户输入的message加上注解@UserMessage
    public Flux<String> chat(@MemoryId String MemoryId,@UserMessage String message);
}
