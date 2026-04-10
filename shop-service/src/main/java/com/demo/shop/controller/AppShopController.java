package com.demo.shop.controller;

import com.literature.common.core.model.ApiResponse;
import com.demo.common.entity.ShopApplication;
import com.demo.shop.mapper.ShopApplicationMapper;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/app/shops")
public class AppShopController {

    private final ShopApplicationMapper shopApplicationMapper;

    public AppShopController(ShopApplicationMapper shopApplicationMapper) {
        this.shopApplicationMapper = shopApplicationMapper;
    }

    @PostMapping("/apply")
    public ApiResponse<Void> apply(@RequestBody ShopApplication req) {
        req.setStatus("PENDING");
        req.setCreateTime(LocalDateTime.now());
        req.setUpdateTime(LocalDateTime.now());
        shopApplicationMapper.insert(req);
        return ApiResponse.success(null);
    }
}
