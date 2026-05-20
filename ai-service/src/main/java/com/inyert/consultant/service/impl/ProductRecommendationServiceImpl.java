package com.inyert.consultant.service.impl;

import com.inyert.consultant.controller.aiservice.ConsultantService;
import com.inyert.consultant.service.ProductRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 商品推荐服务实现
 */
@Service
public class ProductRecommendationServiceImpl implements ProductRecommendationService {

    @Autowired
    private ConsultantService consultantService;

    @Override
    public Flux<String> recommendProducts(String memoryId, String userRequest) {
        return consultantService.chat(memoryId, userRequest);
    }
}
