package com.demo.stock.controller;

import com.demo.common.api.ApiResponse;
import com.demo.stock.dto.ProductCreateRequest;
import com.demo.stock.dto.ProductUpdateRequest;
import com.demo.common.entity.Product;
import com.demo.stock.service.ProductService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ApiResponse<Product> create(@Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.ok("Product created", productService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Product> update(@PathVariable Long id,
                                       @Valid @RequestBody ProductUpdateRequest request) {
        return ApiResponse.ok("Product updated", productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.ok("Product deleted", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> detail(@PathVariable Long id) {
        return ApiResponse.ok(productService.detail(id));
    }

    @GetMapping
    public ApiResponse<List<Product>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(productService.list(keyword, categoryId));
    }
}
