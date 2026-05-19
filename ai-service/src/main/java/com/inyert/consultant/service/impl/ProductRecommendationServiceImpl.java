package com.inyert.consultant.service.impl;

import com.inyert.consultant.controller.aiservice.ConsultantService;
import com.inyert.consultant.service.ProductRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import dev.langchain4j.service.TokenStream;

/**
 * 商品推荐服务实现
 */
@Service
public class ProductRecommendationServiceImpl implements ProductRecommendationService {

    @Autowired
    private ConsultantService consultantService;

    @Override
    public Flux<String> recommendProducts(String memoryId, String userRequest) {
        // 调用 ConsultantService 进行会话和推荐
        // AI 会自动根据用户请求调用 RecommendationTool 中的工具函数
        TokenStream stream = consultantService.chat(memoryId, userRequest);
        return Flux.create(sink -> stream
                .onNext(sink::next)
                .onComplete(response -> sink.complete())
                .onError(sink::error)
                .start());
    }
}
