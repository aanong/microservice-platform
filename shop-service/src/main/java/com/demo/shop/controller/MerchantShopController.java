package com.demo.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.literature.common.core.model.ApiResponse;
import com.demo.common.entity.Shop;
import com.demo.shop.mapper.ShopMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/shop")
public class MerchantShopController {

    private final ShopMapper shopMapper;

    public MerchantShopController(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }

    @GetMapping
    public ApiResponse<Shop> getMyShop(@RequestHeader(value = "shopId", required = false) Long shopId) {
        if (shopId == null) {
            return ApiResponse.error("403", "Not a merchant");
        }
        return ApiResponse.success(shopMapper.selectById(shopId));
    }
}
