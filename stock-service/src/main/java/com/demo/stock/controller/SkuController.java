package com.demo.stock.controller;

import com.demo.common.api.ApiResponse;
import com.demo.common.entity.Sku;
import com.demo.stock.dto.sku.SkuUpdatePriceRequest;
import com.demo.stock.dto.sku.SkuUpdateStatusRequest;
import com.demo.stock.dto.sku.SkuUpdateStockRequest;
import com.demo.stock.service.SkuService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/skus")
public class SkuController {

    private final SkuService skuService;

    public SkuController(SkuService skuService) {
        this.skuService = skuService;
    }

    @GetMapping("/{id}")
    public ApiResponse<Sku> detail(@PathVariable Long id) {
        return ApiResponse.ok(skuService.detail(id));
    }

    @GetMapping
    public ApiResponse<List<Sku>> list(@RequestParam(required = false) Long spuId,
                                       @RequestParam(required = false) Long status) {
        return ApiResponse.ok(skuService.list(spuId, status));
    }

    @PutMapping("/{id}/price")
    public ApiResponse<Sku> updatePrice(@PathVariable Long id, @Valid @RequestBody SkuUpdatePriceRequest request) {
        return ApiResponse.ok("SKU price updated", skuService.updatePrice(id, request.getSalePrice()));
    }

    @PutMapping("/{id}/stock")
    public ApiResponse<Sku> updateStock(@PathVariable Long id, @Valid @RequestBody SkuUpdateStockRequest request) {
        return ApiResponse.ok("SKU stock updated", skuService.updateStock(id, request.getStock()));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Sku> updateStatus(@PathVariable Long id, @Valid @RequestBody SkuUpdateStatusRequest request) {
        return ApiResponse.ok("SKU status updated", skuService.updateStatus(id, request.getStatus()));
    }
}
