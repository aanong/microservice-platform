package com.demo.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.order.constant.BizConstants;
import com.demo.order.dto.CreateCouponTemplateRequest;
import com.demo.order.entity.CouponTemplate;
import com.demo.order.entity.CouponUser;
import com.demo.order.exception.BizException;
import com.demo.order.mapper.CouponTemplateMapper;
import com.demo.order.mapper.CouponUserMapper;
import com.demo.order.service.CouponService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponTemplateMapper couponTemplateMapper;
    private final CouponUserMapper couponUserMapper;

    public CouponServiceImpl(CouponTemplateMapper couponTemplateMapper, CouponUserMapper couponUserMapper) {
        this.couponTemplateMapper = couponTemplateMapper;
        this.couponUserMapper = couponUserMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponTemplate createTemplate(CreateCouponTemplateRequest request) {
        CouponTemplate template = new CouponTemplate();
        template.setName(request.getName());
        template.setType(request.getType());
        template.setThresholdAmount(request.getThresholdAmount());
        template.setDiscountAmount(request.getDiscountAmount());
        template.setSeckillPrice(request.getSeckillPrice());
        template.setSeckillProductId(request.getSeckillProductId());
        template.setTotalCount(request.getTotalCount());
        template.setRemainCount(request.getTotalCount());
        template.setStatus(1);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        couponTemplateMapper.insert(template);
        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponUser receive(Long userId, Long templateId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null || template.getStatus() == null || template.getStatus() != 1) {
            throw new BizException("Coupon template unavailable");
        }
        if (template.getRemainCount() == null || template.getRemainCount() <= 0) {
            throw new BizException("Coupon exhausted");
        }

        template.setRemainCount(template.getRemainCount() - 1);
        template.setUpdateTime(LocalDateTime.now());
        couponTemplateMapper.updateById(template);

        CouponUser couponUser = new CouponUser();
        couponUser.setUserId(userId);
        couponUser.setTemplateId(templateId);
        couponUser.setStatus(BizConstants.COUPON_STATUS_UNUSED);
        couponUser.setCreateTime(LocalDateTime.now());
        couponUser.setUpdateTime(LocalDateTime.now());
        couponUserMapper.insert(couponUser);
        return couponUser;
    }

    @Override
    public List<CouponUser> userCoupons(Long userId) {
        return couponUserMapper.selectList(new LambdaQueryWrapper<CouponUser>()
            .eq(CouponUser::getUserId, userId)
            .orderByDesc(CouponUser::getId));
    }
}
