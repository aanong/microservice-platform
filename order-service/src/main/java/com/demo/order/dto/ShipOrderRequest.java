package com.demo.order.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShipOrderRequest {

    @NotNull(message = "orderId is required")
    private Long orderId;

    @NotBlank(message = "carrierCode is required")
    private String carrierCode;

    @NotBlank(message = "trackingNo is required")
    private String trackingNo;
}
