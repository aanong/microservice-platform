package com.demo.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.order.entity.PromotionActivity;
import com.demo.order.entity.PromotionRule;
import com.demo.order.mapper.PromotionActivityMapper;
import com.demo.order.mapper.PromotionRuleMapper;
import com.demo.order.service.PromotionService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionActivityMapper activityMapper;
    private final PromotionRuleMapper ruleMapper;

    public PromotionServiceImpl(PromotionActivityMapper activityMapper, PromotionRuleMapper ruleMapper) {
        this.activityMapper = activityMapper;
        this.ruleMapper = ruleMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromotionActivity createPromotion(PromotionActivity activity, PromotionRule rule) {
        LocalDateTime now = LocalDateTime.now();
        activity.setCreateTime(now);
        activity.setUpdateTime(now);
        activityMapper.insert(activity);

        rule.setActivityId(activity.getId());
        rule.setCreateTime(now);
        rule.setUpdateTime(now);
        ruleMapper.insert(rule);
        
        return activity;
    }

    @Override
    public PromotionActivity getActivePromotion(Long id) {
        return activityMapper.selectById(id);
    }

    @Override
    public List<PromotionActivity> listActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        return activityMapper.selectList(new LambdaQueryWrapper<PromotionActivity>()
            .eq(PromotionActivity::getStatus, 1)
            .le(PromotionActivity::getStartTime, now)
            .ge(PromotionActivity::getEndTime, now)
            .orderByDesc(PromotionActivity::getId));
    }

    @Override
    public PromotionRule getRuleByActivityId(Long activityId) {
        return ruleMapper.selectOne(new LambdaQueryWrapper<PromotionRule>()
            .eq(PromotionRule::getActivityId, activityId)
            .last("limit 1"));
    }
}
