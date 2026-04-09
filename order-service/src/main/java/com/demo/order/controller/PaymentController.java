package com.demo.order.controller;

import com.demo.common.api.ApiResponse;
import com.demo.order.dto.SimulatePayRequest;
import com.demo.order.entity.OrderMain;
import com.demo.order.security.UserContextHolder;
import com.demo.order.service.PaymentService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/simulate")
    public ApiResponse<OrderMain> simulatePay(@Valid @RequestBody SimulatePayRequest request) {
        return ApiResponse.ok("Payment success", paymentService.simulatePay(UserContextHolder.requireUserId(), request.getOrderId()));
    }
}
