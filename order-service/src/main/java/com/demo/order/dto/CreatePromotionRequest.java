package com.demo.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreatePromotionRequest {
    private String name;
    private String type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal seckillPrice;
    private String skuIds;
}
