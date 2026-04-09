package com.demo.order.service;

import com.demo.order.dto.CreateOrderRequest;
import com.demo.order.dto.OrderDetailResponse;
import com.demo.order.entity.OrderMain;
import java.util.List;

public interface OrderService {

    OrderMain create(CreateOrderRequest request);

    OrderDetailResponse detail(Long orderId, Long userId);

    List<OrderMain> list(Long userId);
}
