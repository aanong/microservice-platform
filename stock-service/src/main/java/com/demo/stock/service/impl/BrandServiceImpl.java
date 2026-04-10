package com.demo.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.common.entity.Brand;
import com.demo.common.entity.Spu;
import com.demo.stock.dto.brand.BrandCreateRequest;
import com.demo.stock.dto.brand.BrandUpdateRequest;
import com.demo.stock.exception.BizException;
import com.demo.stock.mapper.BrandMapper;
import com.demo.stock.mapper.SpuMapper;
import com.demo.stock.service.BrandService;
import com.demo.stock.service.ProductSearchService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandServiceImpl implements BrandService {

    private final BrandMapper brandMapper;
    private final SpuMapper spuMapper;
    private final ProductSearchService productSearchService;

    public BrandServiceImpl(BrandMapper brandMapper,
                            SpuMapper spuMapper,
                            ProductSearchService productSearchService) {
        this.brandMapper = brandMapper;
        this.spuMapper = spuMapper;
        this.productSearchService = productSearchService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Brand create(BrandCreateRequest request) {
        checkCodeExists(request.getCode(), null);
        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setCode(request.getCode());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setSort(request.getSort());
        brand.setStatus(request.getStatus());
        brand.setRemark(request.getRemark());
        brand.setCreateTime(LocalDateTime.now());
        brand.setUpdateTime(LocalDateTime.now());
        brandMapper.insert(brand);
        return brand;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Brand update(Long id, BrandUpdateRequest request) {
        Brand brand = requireBrand(id);
        checkCodeExists(request.getCode(), id);
        brand.setName(request.getName());
        brand.setCode(request.getCode());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setSort(request.getSort());
        brand.setStatus(request.getStatus());
        brand.setRemark(request.getRemark());
        brand.setUpdateTime(LocalDateTime.now());
        brandMapper.updateById(brand);
        productSearchService.rebuildByBrand(id);
        return brand;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireBrand(id);
        Long spuCount = spuMapper.selectCount(new LambdaQueryWrapper<Spu>().eq(Spu::getBrandId, id));
        if (spuCount != null && spuCount > 0L) {
            throw new BizException("Brand has SPU and cannot be deleted");
        }
        brandMapper.deleteById(id);
    }

    @Override
    public Brand detail(Long id) {
        return requireBrand(id);
    }

    @Override
    public List<Brand> list(String keyword) {
        LambdaQueryWrapper<Brand> query = new LambdaQueryWrapper<Brand>()
            .orderByAsc(Brand::getSort)
            .orderByDesc(Brand::getId);
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.like(Brand::getName, keyword.trim());
        }
        return brandMapper.selectList(query);
    }

    private Brand requireBrand(Long id) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null) {
            throw new BizException("Brand not found, id=" + id);
        }
        return brand;
    }

    private void checkCodeExists(String code, Long excludeId) {
        LambdaQueryWrapper<Brand> query = new LambdaQueryWrapper<Brand>().eq(Brand::getCode, code);
        if (excludeId != null) {
            query.ne(Brand::getId, excludeId);
        }
        Long count = brandMapper.selectCount(query);
        if (count != null && count > 0L) {
            throw new BizException("Brand code already exists: " + code);
        }
    }
}
