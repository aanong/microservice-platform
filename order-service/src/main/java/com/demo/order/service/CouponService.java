package com.demo.order.service;

import com.demo.order.dto.CreateCouponTemplateRequest;
import com.demo.order.entity.CouponTemplate;
import com.demo.order.entity.CouponUser;
import java.util.List;

public interface CouponService {

    CouponTemplate createTemplate(CreateCouponTemplateRequest request);

    CouponUser receive(Long userId, Long templateId);

    List<CouponUser> userCoupons(Long userId);
}
