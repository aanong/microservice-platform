package com.demo.order.service;

import com.demo.order.entity.PromotionActivity;
import com.demo.order.entity.PromotionRule;
import java.util.List;

public interface PromotionService {
    PromotionActivity createPromotion(PromotionActivity activity, PromotionRule rule);
    PromotionActivity getActivePromotion(Long id);
    List<PromotionActivity> listActivePromotions();
    PromotionRule getRuleByActivityId(Long activityId);
}
