package com.demo.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("refund_item")
public class RefundItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long refundId;
    private Long orderItemId;
    private Integer quantity;
    private BigDecimal refundAmount;
    private LocalDateTime createTime;
}
