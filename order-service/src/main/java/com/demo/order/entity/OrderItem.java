package com.demo.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long productId;
    private String productName;
    private String skuCode;
    private BigDecimal salePrice;
    private Integer quantity;
    private BigDecimal totalAmount;
    private BigDecimal realPayAmount;
    private Integer refundedQuantity;
    private BigDecimal refundedAmount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
