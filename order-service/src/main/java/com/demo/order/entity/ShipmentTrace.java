package com.demo.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("shipment_trace")
public class ShipmentTrace {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shipmentId;
    private String traceStatus;
    private String content;
    private LocalDateTime traceTime;
    private LocalDateTime createTime;
}
