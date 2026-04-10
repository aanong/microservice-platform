package com.demo.order.controller;

import com.literature.common.core.model.ApiResponse;
import com.demo.order.dto.CreatePromotionRequest;
import com.demo.order.entity.PromotionActivity;
import com.demo.order.entity.PromotionRule;
import com.demo.order.service.PromotionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/merchant/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    public ApiResponse<PromotionActivity> create(@RequestBody CreatePromotionRequest request) {
        PromotionActivity activity = new PromotionActivity();
        activity.setName(request.getName());
        activity.setType(request.getType());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setStatus(1);

        PromotionRule rule = new PromotionRule();
        rule.setThresholdAmount(request.getThresholdAmount());
        rule.setDiscountAmount(request.getDiscountAmount());
        rule.setSeckillPrice(request.getSeckillPrice());
        rule.setSkuIds(request.getSkuIds());

        PromotionActivity created = promotionService.createPromotion(activity, rule);
        return ApiResponse.success(created);
    }

    @GetMapping("/active")
    public ApiResponse<List<PromotionActivity>> listActive() {
        return ApiResponse.success(promotionService.listActivePromotions());
    }
}
