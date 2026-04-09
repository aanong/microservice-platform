package com.demo.order.mapper;

import com.demo.common.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductStockMapper {

    Product selectById(@Param("id") Long id);

    int deductStock(@Param("id") Long id, @Param("qty") Integer qty);

    int addStock(@Param("id") Long id, @Param("qty") Integer qty);
}
