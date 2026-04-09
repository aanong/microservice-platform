package com.demo.order.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundRequest {

    @NotNull(message = "orderId is required")
    private Long orderId;

    private Long userId;

    private Long orderItemId;

    private Integer quantity;

    private String reason;
}
