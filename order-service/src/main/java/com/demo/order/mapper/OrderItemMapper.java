package com.demo.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.order.entity.OrderItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
