package com.demo.order.controller;

import com.demo.common.api.ApiResponse;
import com.demo.order.dto.CreateCouponTemplateRequest;
import com.demo.order.dto.ReceiveCouponRequest;
import com.demo.order.entity.CouponTemplate;
import com.demo.order.entity.CouponUser;
import com.demo.order.security.UserContextHolder;
import com.demo.order.service.CouponService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/templates")
    public ApiResponse<CouponTemplate> createTemplate(@Valid @RequestBody CreateCouponTemplateRequest request) {
        return ApiResponse.ok("Template created", couponService.createTemplate(request));
    }

    @PostMapping("/receive")
    public ApiResponse<CouponUser> receive(@Valid @RequestBody ReceiveCouponRequest request) {
        return ApiResponse.ok("Coupon received", couponService.receive(UserContextHolder.requireUserId(), request.getTemplateId()));
    }

    @GetMapping("/user")
    public ApiResponse<List<CouponUser>> userCoupons() {
        return ApiResponse.ok(couponService.userCoupons(UserContextHolder.requireUserId()));
    }
}
