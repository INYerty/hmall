package com.inyert.consultant.controller;

import com.inyert.consultant.service.ProductRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 商品推荐控制器
 */
@RestController
@RequestMapping("/recommend")
public class RecommendationController {

    @Autowired
    private ProductRecommendationService productRecommendationService;

    /**
     * 商品推荐接口
     * @param memoryId 会话ID，可用于保持对话上下文
     * @param userRequest 用户的推荐需求，例如："推荐一个性价比高的手机"
     * @return 推荐结果流
     */
    @GetMapping(value = "/products", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> recommendProducts(
            @RequestParam(required = false, defaultValue = "default") String memoryId,
            @RequestParam String userRequest) {
        return productRecommendationService.recommendProducts(memoryId, userRequest);
    }
}

