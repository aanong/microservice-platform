package com.demo.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.common.entity.Sku;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SkuMapper extends BaseMapper<Sku> {
}
