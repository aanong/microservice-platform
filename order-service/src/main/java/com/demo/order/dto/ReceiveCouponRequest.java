package com.demo.order.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReceiveCouponRequest {

    @NotNull(message = "templateId is required")
    private Long templateId;
}
