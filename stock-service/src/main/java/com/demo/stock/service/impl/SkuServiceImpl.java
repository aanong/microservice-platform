package com.demo.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.common.entity.Sku;
import com.literature.common.core.exception.BizException;
import com.literature.common.core.model.ErrorCode;
import com.demo.stock.mapper.SkuMapper;
import com.demo.stock.service.SkuService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkuServiceImpl implements SkuService {

    private final SkuMapper skuMapper;

    public SkuServiceImpl(SkuMapper skuMapper) {
        this.skuMapper = skuMapper;
    }

    @Override
    public Sku detail(Long id) {
        return requireSku(id);
    }

    @Override
    public List<Sku> list(Long spuId, Long status) {
        LambdaQueryWrapper<Sku> query = new LambdaQueryWrapper<>();
        if (spuId != null) {
            query.eq(Sku::getSpuId, spuId);
        }
        if (status != null) {
            query.eq(Sku::getStatus, status.intValue());
        }
        query.orderByAsc(Sku::getId);
        return skuMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Sku updatePrice(Long id, BigDecimal salePrice) {
        Sku sku = requireSku(id);
        sku.setSalePrice(salePrice);
        sku.setUpdateTime(LocalDateTime.now());
        skuMapper.updateById(sku);
        return sku;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Sku updateStock(Long id, Integer stock) {
        Sku sku = requireSku(id);
        sku.setStock(stock);
        sku.setUpdateTime(LocalDateTime.now());
        skuMapper.updateById(sku);
        return sku;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Sku updateStatus(Long id, Integer status) {
        Sku sku = requireSku(id);
        sku.setStatus(status);
        sku.setUpdateTime(LocalDateTime.now());
        skuMapper.updateById(sku);
        return sku;
    }

    private Sku requireSku(Long id) {
        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Sku not found, id=" + id);
        }
        return sku;
    }
}
