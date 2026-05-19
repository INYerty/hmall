package com.inyert.consultant.controller;


import com.inyert.consultant.controller.aiservice.ConsultantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import dev.langchain4j.service.TokenStream;

@RestController
public class ChatController {

//    @Autowired
//    private OpenAiChatModel model;
//
//
//    /**
//     * 聊天
//     * @param message 浏览器传递的用户问题
//     * @return
//     */
//
//    @RequestMapping("/chat")
//    public String chat(String message){
//        return model.chat(message);
//    }

    @Autowired
    private ConsultantService consultantService;
    // 改这一行
    @RequestMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> chat(@RequestParam String memoryId, @RequestParam String message) {
        TokenStream stream = consultantService.chat(memoryId, message);
        return Flux.create(sink -> stream
                .onNext(sink::next)  // ← 直接这样
                .onComplete(response -> sink.complete())
                .onError(sink::error)
                .start());
    }

}
