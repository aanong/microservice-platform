package com.demo.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("refund_main")
public class RefundMain {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String refundNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String refundType;
    private String refundStatus;
    private BigDecimal refundAmount;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
