package com.demo.order.controller;

import com.demo.common.api.ApiResponse;
import com.demo.order.dto.CreateOrderRequest;
import com.demo.order.dto.OrderDetailResponse;
import com.demo.order.entity.OrderMain;
import com.demo.order.security.UserContextHolder;
import com.demo.order.service.OrderService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderMain> create(@Valid @RequestBody CreateOrderRequest request) {
        request.setUserId(UserContextHolder.requireUserId());
        return ApiResponse.ok("Order created", orderService.create(request));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> detail(@PathVariable Long orderId) {
        return ApiResponse.ok(orderService.detail(orderId, UserContextHolder.requireUserId()));
    }

    @GetMapping
    public ApiResponse<List<OrderMain>> list() {
        return ApiResponse.ok(orderService.list(UserContextHolder.requireUserId()));
    }
}
