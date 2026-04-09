package com.demo.order.service;

import com.demo.order.entity.OrderMain;

public interface PaymentService {

    OrderMain simulatePay(Long userId, Long orderId);
}
