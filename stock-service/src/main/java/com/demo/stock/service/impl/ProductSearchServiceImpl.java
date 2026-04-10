package com.demo.stock.service.impl;

import com.demo.stock.dto.sku.ProductSearchItem;
import com.demo.stock.service.ProductSearchService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private static final Logger log = LoggerFactory.getLogger(ProductSearchServiceImpl.class);

    @Override
    public List<ProductSearchItem> search(String keyword, Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice, Integer status, Integer pageNo, Integer pageSize) {
        log.warn("ProductSearchService#search is not implemented fully yet.");
        return Collections.emptyList();
    }

    @Override
    public void upsertSku(Long skuId) {
        log.info("Mock upsertSku: {}", skuId);
    }

    @Override
    public void deleteSku(Long skuId) {
        log.info("Mock deleteSku: {}", skuId);
    }

    @Override
    public void rebuildBySpu(Long spuId) {
        log.info("Mock rebuildBySpu: {}", spuId);
    }

    @Override
    public void rebuildByCategory(Long categoryId) {
        log.info("Mock rebuildByCategory: {}", categoryId);
    }

    @Override
    public void rebuildByBrand(Long brandId) {
        log.info("Mock rebuildByBrand: {}", brandId);
    }

    @Override
    public void rebuildAll() {
        log.info("Mock rebuildAll");
    }
}
