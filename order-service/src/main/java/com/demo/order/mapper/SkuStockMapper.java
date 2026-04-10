package com.demo.order.mapper;

import com.demo.common.entity.Sku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SkuStockMapper {

    Sku selectById(@Param("id") Long id);

    int deductStock(@Param("id") Long id, @Param("qty") Integer qty);

    int addStock(@Param("id") Long id, @Param("qty") Integer qty);
}
