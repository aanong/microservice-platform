package com.demo.order.controller;

import com.demo.common.api.ApiResponse;
import com.demo.order.dto.RefundRequest;
import com.demo.order.entity.RefundMain;
import com.demo.order.security.UserContextHolder;
import com.demo.order.service.RefundService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    public ApiResponse<RefundMain> refund(@Valid @RequestBody RefundRequest request) {
        request.setUserId(UserContextHolder.requireUserId());
        return ApiResponse.ok("Refund success", refundService.refund(request));
    }
}
