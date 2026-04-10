package com.demo.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("coupon_template")
public class CouponTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal seckillPrice;
    private Long seckillSkuId;
    private Long seckillProductId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalCount;
    private Integer remainCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
