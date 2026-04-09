package com.demo.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.common.entity.Category;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    List<Category> selectByKeyword(@Param("keyword") String keyword);
}
