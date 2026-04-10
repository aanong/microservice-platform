package com.demo.stock.service;

import com.demo.common.entity.Brand;
import com.demo.stock.dto.brand.BrandCreateRequest;
import com.demo.stock.dto.brand.BrandUpdateRequest;
import java.util.List;

public interface BrandService {

    Brand create(BrandCreateRequest request);

    Brand update(Long id, BrandUpdateRequest request);

    void delete(Long id);

    Brand detail(Long id);

    List<Brand> list(String keyword);
}
