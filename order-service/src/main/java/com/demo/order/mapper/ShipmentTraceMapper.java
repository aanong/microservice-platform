package com.demo.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.order.entity.ShipmentTrace;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShipmentTraceMapper extends BaseMapper<ShipmentTrace> {

    List<ShipmentTrace> selectByShipmentId(@Param("shipmentId") Long shipmentId);
}
