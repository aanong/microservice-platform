package com.demo.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("promotion_rule")
public class PromotionRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal seckillPrice;
    private String skuIds; // JSON array of SKU IDs
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
