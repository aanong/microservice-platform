package com.demo.stock.controller;

import com.demo.common.api.ApiResponse;
import com.demo.common.entity.Brand;
import com.demo.stock.dto.brand.BrandCreateRequest;
import com.demo.stock.dto.brand.BrandUpdateRequest;
import com.demo.stock.service.BrandService;
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
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    public ApiResponse<Brand> create(@Valid @RequestBody BrandCreateRequest request) {
        return ApiResponse.ok("Brand created", brandService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Brand> update(@PathVariable Long id, @Valid @RequestBody BrandUpdateRequest request) {
        return ApiResponse.ok("Brand updated", brandService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        brandService.delete(id);
        return ApiResponse.ok("Brand deleted", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<Brand> detail(@PathVariable Long id) {
        return ApiResponse.ok(brandService.detail(id));
    }

    @GetMapping
    public ApiResponse<List<Brand>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(brandService.list(keyword));
    }
}
