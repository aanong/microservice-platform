package com.demo.order.dto;

import java.util.List;
import lombok.Data;

@Data
public class CreateOrderRequest {

    private Long userId;

    private List<Long> cartItemIds;

    private Long couponUserId;
}
