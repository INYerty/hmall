package com.inyert.consultant.service;

import reactor.core.publisher.Flux;

/**
 * 商品推荐服务接口
 */
public interface ProductRecommendationService {

    /**
     * 根据用户输入推荐商品
     *
     * @param memoryId    会话ID，用于保持对话上下文
     * @param userRequest 用户的推荐请求，如"推荐一个好用的手机"
     * @return 推荐结果流
     */
    Flux<String> recommendProducts(String memoryId, String userRequest);
}

