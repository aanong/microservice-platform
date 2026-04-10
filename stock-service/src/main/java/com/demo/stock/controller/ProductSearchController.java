package com.demo.stock.controller;

import com.demo.common.api.ApiResponse;
import com.demo.stock.dto.sku.ProductSearchItem;
import com.demo.stock.service.ProductSearchService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    public ProductSearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @GetMapping("/search")
    public ApiResponse<List<ProductSearchItem>> search(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) Long categoryId,
                                                       @RequestParam(required = false) Long brandId,
                                                       @RequestParam(required = false) BigDecimal minPrice,
                                                       @RequestParam(required = false) BigDecimal maxPrice,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                                       @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        return ApiResponse.ok(productSearchService.search(
            keyword, categoryId, brandId, minPrice, maxPrice, status, pageNo, pageSize));
    }
}
