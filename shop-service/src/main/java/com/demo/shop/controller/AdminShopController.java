package com.demo.shop.controller;

import com.literature.common.core.model.ApiResponse;
import com.demo.common.entity.Shop;
import com.demo.shop.mapper.ShopMapper;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/shops")
public class AdminShopController {

    private final ShopMapper shopMapper;

    public AdminShopController(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }

    @GetMapping
    public ApiResponse<List<Shop>> listAll() {
        return ApiResponse.success(shopMapper.selectList(null));
    }

    @PostMapping("/{id}/audit")
    public ApiResponse<Void> audit(@PathVariable Long id, @RequestParam Integer status) {
        Shop shop = shopMapper.selectById(id);
        if (shop != null) {
            shop.setAuditStatus(status);
            shopMapper.updateById(shop);
        }
        return ApiResponse.success(null);
    }
}
