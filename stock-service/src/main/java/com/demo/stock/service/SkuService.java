package com.demo.stock.service;

import com.demo.common.entity.Sku;
import java.util.List;

public interface SkuService {

    Sku detail(Long id);

    List<Sku> list(Long spuId, Long status);

    Sku updatePrice(Long id, java.math.BigDecimal salePrice);

    Sku updateStock(Long id, Integer stock);

    Sku updateStatus(Long id, Integer status);
}
