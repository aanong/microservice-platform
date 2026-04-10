package com.demo.stock.service;

import com.demo.stock.dto.sku.ProductSearchItem;
import java.math.BigDecimal;
import java.util.List;

public interface ProductSearchService {

    List<ProductSearchItem> search(String keyword,
                                   Long categoryId,
                                   Long brandId,
                                   BigDecimal minPrice,
                                   BigDecimal maxPrice,
                                   Integer status,
                                   Integer pageNo,
                                   Integer pageSize);

    void upsertSku(Long skuId);

    void deleteSku(Long skuId);

    void rebuildBySpu(Long spuId);

    void rebuildByCategory(Long categoryId);

    void rebuildByBrand(Long brandId);

    void rebuildAll();
}
