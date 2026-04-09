package com.demo.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.order.entity.CartItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    List<CartItem> selectByUserAndIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
