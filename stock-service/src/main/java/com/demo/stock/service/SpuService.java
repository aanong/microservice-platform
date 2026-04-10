package com.demo.stock.service;

import com.demo.common.entity.Spu;
import com.demo.stock.dto.spu.SpuCreateRequest;
import com.demo.stock.dto.spu.SpuDetailResponse;
import com.demo.stock.dto.spu.SpuUpdateRequest;
import java.util.List;

public interface SpuService {

    Spu create(SpuCreateRequest request);

    Spu update(Long id, SpuUpdateRequest request);

    SpuDetailResponse detail(Long id);

    List<Spu> list(String keyword, Long categoryId, Long brandId);

    void generateSkus(Long spuId);
}
