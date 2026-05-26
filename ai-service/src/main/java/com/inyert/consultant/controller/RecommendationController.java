package com.inyert.consultant.controller;

import com.hmall.api.dto.ItemDTO;
import com.inyert.consultant.service.ProductRecommendationService;
import com.inyert.consultant.tool.RecommendationTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * 商品推荐控制器
 */
@RestController
@RequestMapping("/recommend")
public class RecommendationController {

    @Autowired
    private ProductRecommendationService productRecommendationService;

    @Autowired
    private RecommendationTool recommendationTool;

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

    /**
     * 获取示例推荐商品列表（用于前端展示）
     */
    @GetMapping("/items")
    public Mono<List<ItemDTO>> recommendItems(@RequestParam(required = false) Integer maxPrice,
                                              @RequestParam(required = false) Integer minPrice) {
        return Mono.fromCallable(() -> {
                    List<ItemDTO> items = recommendationTool.querySampleItems();
                    if (items == null || items.isEmpty()) {
                        return items;
                    }
                    if (minPrice != null && minPrice > 0) {
                        final int minPriceInCents = minPrice * 100;
                        items = items.stream()
                                .filter(item -> item.getPrice() != null && item.getPrice() >= minPriceInCents)
                                .toList();
                    }
                    if (maxPrice != null && maxPrice > 0) {
                        final int maxPriceInCents = maxPrice * 100;
                        items = items.stream()
                                .filter(item -> item.getPrice() != null && item.getPrice() <= maxPriceInCents)
                                .toList();
                    }
                    return items;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
