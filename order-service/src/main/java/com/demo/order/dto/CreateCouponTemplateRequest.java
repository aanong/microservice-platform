package com.demo.order.dto;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCouponTemplateRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "type is required")
    private String type;

    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal seckillPrice;
    private Long seckillProductId;

    @NotNull(message = "totalCount is required")
    private Integer totalCount;
}
