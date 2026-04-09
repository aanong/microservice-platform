package com.demo.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.common.entity.Product;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    List<Product> selectByCondition(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);
}
