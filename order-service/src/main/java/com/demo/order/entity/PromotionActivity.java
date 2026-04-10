package com.demo.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("promotion_activity")
public class PromotionActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type; // FLASH_SALE, FULL_REDUCTION, DIRECT_REDUCTION
    private String ownerType; // PLATFORM, SHOP
    private Long shopId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status; // 1: active, 0: inactive
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
