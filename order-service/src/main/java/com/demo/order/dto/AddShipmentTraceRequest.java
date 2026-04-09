package com.demo.order.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddShipmentTraceRequest {

    @NotNull(message = "shipmentId is required")
    private Long shipmentId;

    @NotBlank(message = "traceStatus is required")
    private String traceStatus;

    @NotBlank(message = "content is required")
    private String content;
}
