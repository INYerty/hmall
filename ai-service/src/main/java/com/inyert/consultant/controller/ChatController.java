package com.inyert.consultant.controller;


import com.inyert.consultant.controller.aiservice.ConsultantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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
    @RequestMapping(value = "/chat",produces = "text/html;charset=UTF-8")
    public Flux<String> chat(String memoryId,String message){
        return consultantService.chat(memoryId,message);
    }

}
