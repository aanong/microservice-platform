package com.demo.order.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SimulatePayRequest {

    @NotNull(message = "orderId is required")
    private Long orderId;
}
