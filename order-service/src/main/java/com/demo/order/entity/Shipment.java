package com.demo.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("shipment")
public class Shipment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private String carrierCode;
    private String trackingNo;
    private String shipmentStatus;
    private LocalDateTime shippedTime;
    private LocalDateTime signedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
