package com.demo.stock.controller;

import com.demo.common.api.ApiResponse;
import com.demo.common.entity.Spu;
import com.demo.stock.dto.spu.SpuCreateRequest;
import com.demo.stock.dto.spu.SpuDetailResponse;
import com.demo.stock.dto.spu.SpuUpdateRequest;
import com.demo.stock.service.SpuService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/spus")
public class SpuController {

    private final SpuService spuService;

    public SpuController(SpuService spuService) {
        this.spuService = spuService;
    }

    @PostMapping
    public ApiResponse<Spu> create(@Valid @RequestBody SpuCreateRequest request) {
        return ApiResponse.ok("SPU created", spuService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Spu> update(@PathVariable Long id, @Valid @RequestBody SpuUpdateRequest request) {
        return ApiResponse.ok("SPU updated", spuService.update(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<SpuDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(spuService.detail(id));
    }

    @GetMapping
    public ApiResponse<List<Spu>> list(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) Long brandId) {
        return ApiResponse.ok(spuService.list(keyword, categoryId, brandId));
    }

    @PostMapping("/{spuId}/skus/generate")
    public ApiResponse<Void> generateSkus(@PathVariable Long spuId) {
        spuService.generateSkus(spuId);
        return ApiResponse.ok("SKU generated", null);
    }
}
