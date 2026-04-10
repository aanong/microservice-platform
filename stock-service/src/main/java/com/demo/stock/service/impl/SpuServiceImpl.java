package com.demo.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.common.entity.Brand;
import com.demo.common.entity.Category;
import com.demo.common.entity.Sku;
import com.demo.common.entity.Spu;
import com.demo.common.entity.SpuSpec;
import com.demo.common.entity.SpuSpecValue;
import com.demo.stock.dto.spu.SpuCreateRequest;
import com.demo.stock.dto.spu.SpuDetailResponse;
import com.demo.stock.dto.spu.SpuSpecItem;
import com.demo.stock.dto.spu.SpuSpecRequest;
import com.demo.stock.dto.spu.SpuUpdateRequest;
import com.demo.stock.exception.BizException;
import com.demo.stock.mapper.BrandMapper;
import com.demo.stock.mapper.CategoryMapper;
import com.demo.stock.mapper.SkuMapper;
import com.demo.stock.mapper.SpuMapper;
import com.demo.stock.mapper.SpuSpecMapper;
import com.demo.stock.mapper.SpuSpecValueMapper;
import com.demo.stock.service.ProductSearchService;
import com.demo.stock.service.SpuService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpuServiceImpl implements SpuService {

    private static final int SKU_STATUS_DISABLED = 0;

    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final SpuSpecMapper spuSpecMapper;
    private final SpuSpecValueMapper spuSpecValueMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final ProductSearchService productSearchService;
    private final ObjectMapper objectMapper;

    public SpuServiceImpl(SpuMapper spuMapper,
                          SkuMapper skuMapper,
                          SpuSpecMapper spuSpecMapper,
                          SpuSpecValueMapper spuSpecValueMapper,
                          CategoryMapper categoryMapper,
                          BrandMapper brandMapper,
                          ProductSearchService productSearchService,
                          ObjectMapper objectMapper) {
        this.spuMapper = spuMapper;
        this.skuMapper = skuMapper;
        this.spuSpecMapper = spuSpecMapper;
        this.spuSpecValueMapper = spuSpecValueMapper;
        this.categoryMapper = categoryMapper;
        this.brandMapper = brandMapper;
        this.productSearchService = productSearchService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Spu create(SpuCreateRequest request) {
        requireCategory(request.getCategoryId());
        requireBrand(request.getBrandId());

        Spu spu = new Spu();
        spu.setSpuCode(generateSpuCode());
        fillSpu(spu, request.getName(), request.getCategoryId(), request.getBrandId(), request.getMainImageUrl(),
            request.getDetailImages(), request.getDescription(), request.getStatus());
        spuMapper.insert(spu);

        saveSpecs(spu.getId(), request.getSpecs());
        generateSkus(spu.getId());
        return spu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Spu update(Long id, SpuUpdateRequest request) {
        Spu spu = requireSpu(id);
        requireCategory(request.getCategoryId());
        requireBrand(request.getBrandId());

        fillSpu(spu, request.getName(), request.getCategoryId(), request.getBrandId(), request.getMainImageUrl(),
            request.getDetailImages(), request.getDescription(), request.getStatus());
        spu.setUpdateTime(LocalDateTime.now());
        spuMapper.updateById(spu);

        saveSpecs(id, request.getSpecs());
        generateSkus(id);
        productSearchService.rebuildBySpu(id);
        return spu;
    }

    @Override
    public SpuDetailResponse detail(Long id) {
        Spu spu = requireSpu(id);
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
            .eq(Sku::getSpuId, id)
            .orderByAsc(Sku::getId));

        SpuDetailResponse response = new SpuDetailResponse();
        response.setSpu(spu);
        response.setSpecs(loadSpecItems(id));
        response.setSkus(skus);
        return response;
    }

    @Override
    public List<Spu> list(String keyword, Long categoryId, Long brandId) {
        LambdaQueryWrapper<Spu> query = new LambdaQueryWrapper<Spu>()
            .orderByDesc(Spu::getId);
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.like(Spu::getName, keyword.trim());
        }
        if (categoryId != null) {
            query.eq(Spu::getCategoryId, categoryId);
        }
        if (brandId != null) {
            query.eq(Spu::getBrandId, brandId);
        }
        return spuMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateSkus(Long spuId) {
        Spu spu = requireSpu(spuId);
        List<SpuSpec> specs = spuSpecMapper.selectList(new LambdaQueryWrapper<SpuSpec>()
            .eq(SpuSpec::getSpuId, spuId)
            .orderByAsc(SpuSpec::getSort)
            .orderByAsc(SpuSpec::getId));

        if (specs == null || specs.isEmpty()) {
            throw new BizException("SPU specs is empty");
        }

        List<SpecDimension> dimensions = new ArrayList<SpecDimension>();
        for (SpuSpec spec : specs) {
            List<SpuSpecValue> values = spuSpecValueMapper.selectList(new LambdaQueryWrapper<SpuSpecValue>()
                .eq(SpuSpecValue::getSpuSpecId, spec.getId())
                .orderByAsc(SpuSpecValue::getSort)
                .orderByAsc(SpuSpecValue::getId));
            if (values == null || values.isEmpty()) {
                throw new BizException("Spec has no values: " + spec.getSpecName());
            }
            List<String> valueList = values.stream().map(SpuSpecValue::getSpecValue).collect(Collectors.toList());
            dimensions.add(new SpecDimension(spec.getSpecName(), valueList));
        }

        List<Map<String, String>> combinations = new ArrayList<Map<String, String>>();
        buildCartesian(dimensions, 0, new LinkedHashMap<String, String>(), combinations);

        List<Sku> current = skuMapper.selectList(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spuId));
        Map<String, Sku> currentBySignature = new HashMap<String, Sku>();
        for (Sku sku : current) {
            currentBySignature.put(sku.getSpecSignature(), sku);
        }

        Set<String> newSignatures = new LinkedHashSet<String>();
        int nextSeq = current.size() + 1;
        for (Map<String, String> combo : combinations) {
            String signature = buildSignature(combo);
            newSignatures.add(signature);
            if (currentBySignature.containsKey(signature)) {
                continue;
            }
            Sku sku = new Sku();
            sku.setSpuId(spuId);
            sku.setSkuCode(generateSkuCode(spu.getSpuCode(), nextSeq++));
            sku.setSpecSignature(signature);
            sku.setSpecJson(toJson(combo));
            sku.setSalePrice(BigDecimal.ZERO);
            sku.setStock(0);
            sku.setStatus(SKU_STATUS_DISABLED);
            sku.setMainImageUrl(spu.getMainImageUrl());
            sku.setCreateTime(LocalDateTime.now());
            sku.setUpdateTime(LocalDateTime.now());
            skuMapper.insert(sku);
            productSearchService.upsertSku(sku.getId());
        }

        for (Sku sku : current) {
            if (newSignatures.contains(sku.getSpecSignature())) {
                productSearchService.upsertSku(sku.getId());
                continue;
            }
            sku.setStatus(SKU_STATUS_DISABLED);
            sku.setUpdateTime(LocalDateTime.now());
            skuMapper.updateById(sku);
            productSearchService.upsertSku(sku.getId());
        }
    }

    private void fillSpu(Spu spu,
                         String name,
                         Long categoryId,
                         Long brandId,
                         String mainImageUrl,
                         List<String> detailImages,
                         String description,
                         Integer status) {
        spu.setName(name);
        spu.setCategoryId(categoryId);
        spu.setBrandId(brandId);
        spu.setMainImageUrl(mainImageUrl);
        spu.setDetailImages(toJson(detailImages == null ? Collections.<String>emptyList() : detailImages));
        spu.setDescription(description);
        spu.setStatus(status);
        if (spu.getCreateTime() == null) {
            spu.setCreateTime(LocalDateTime.now());
        }
        if (spu.getUpdateTime() == null) {
            spu.setUpdateTime(LocalDateTime.now());
        }
    }

    private void saveSpecs(Long spuId, List<SpuSpecRequest> specRequests) {
        List<SpuSpec> exists = spuSpecMapper.selectList(new LambdaQueryWrapper<SpuSpec>().eq(SpuSpec::getSpuId, spuId));
        if (exists != null && !exists.isEmpty()) {
            List<Long> specIds = exists.stream().map(SpuSpec::getId).collect(Collectors.toList());
            spuSpecValueMapper.delete(new LambdaQueryWrapper<SpuSpecValue>().in(SpuSpecValue::getSpuSpecId, specIds));
            spuSpecMapper.delete(new LambdaQueryWrapper<SpuSpec>().eq(SpuSpec::getSpuId, spuId));
        }

        if (specRequests == null || specRequests.isEmpty()) {
            return;
        }

        int idx = 0;
        for (SpuSpecRequest request : specRequests) {
            if (request.getValues() == null || request.getValues().isEmpty()) {
                throw new BizException("spec values cannot be empty: " + request.getSpecName());
            }
            SpuSpec spec = new SpuSpec();
            spec.setSpuId(spuId);
            spec.setSpecName(request.getSpecName());
            spec.setSort(request.getSort() == null ? idx : request.getSort());
            spec.setCreateTime(LocalDateTime.now());
            spec.setUpdateTime(LocalDateTime.now());
            spuSpecMapper.insert(spec);

            int valueSort = 0;
            for (String value : request.getValues()) {
                if (value == null || value.trim().isEmpty()) {
                    continue;
                }
                SpuSpecValue specValue = new SpuSpecValue();
                specValue.setSpuSpecId(spec.getId());
                specValue.setSpecValue(value.trim());
                specValue.setSort(valueSort++);
                specValue.setCreateTime(LocalDateTime.now());
                specValue.setUpdateTime(LocalDateTime.now());
                spuSpecValueMapper.insert(specValue);
            }
            idx++;
        }
    }

    private List<SpuSpecItem> loadSpecItems(Long spuId) {
        List<SpuSpec> specs = spuSpecMapper.selectList(new LambdaQueryWrapper<SpuSpec>()
            .eq(SpuSpec::getSpuId, spuId)
            .orderByAsc(SpuSpec::getSort)
            .orderByAsc(SpuSpec::getId));
        List<SpuSpecItem> items = new ArrayList<SpuSpecItem>();
        for (SpuSpec spec : specs) {
            List<SpuSpecValue> values = spuSpecValueMapper.selectList(new LambdaQueryWrapper<SpuSpecValue>()
                .eq(SpuSpecValue::getSpuSpecId, spec.getId())
                .orderByAsc(SpuSpecValue::getSort)
                .orderByAsc(SpuSpecValue::getId));
            SpuSpecItem item = new SpuSpecItem();
            item.setSpecName(spec.getSpecName());
            item.setValues(values.stream().map(SpuSpecValue::getSpecValue).collect(Collectors.toList()));
            items.add(item);
        }
        return items;
    }

    private void buildCartesian(List<SpecDimension> dimensions,
                                int idx,
                                Map<String, String> current,
                                List<Map<String, String>> output) {
        if (idx >= dimensions.size()) {
            output.add(new LinkedHashMap<String, String>(current));
            return;
        }
        SpecDimension dimension = dimensions.get(idx);
        for (String value : dimension.values) {
            current.put(dimension.name, value);
            buildCartesian(dimensions, idx + 1, current, output);
            current.remove(dimension.name);
        }
    }

    private String buildSignature(Map<String, String> combo) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : combo.entrySet()) {
            if (sb.length() > 0) {
                sb.append('|');
            }
            sb.append(entry.getKey()).append(':').append(entry.getValue());
        }
        return sb.toString();
    }

    private String generateSpuCode() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);
        Long count = spuMapper.selectCount(new LambdaQueryWrapper<Spu>()
            .ge(Spu::getCreateTime, start)
            .le(Spu::getCreateTime, end));
        long seq = (count == null ? 0L : count) + 1L;
        return "SPU" + today.format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%06d", seq);
    }

    private String generateSkuCode(String spuCode, int seq) {
        String suffix = spuCode == null ? "00000000" : spuCode.substring(Math.max(0, spuCode.length() - 8));
        return "SKU" + suffix + String.format("%04d", seq);
    }

    private Category requireCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BizException("Category not found, id=" + categoryId);
        }
        return category;
    }

    private Brand requireBrand(Long brandId) {
        Brand brand = brandMapper.selectById(brandId);
        if (brand == null) {
            throw new BizException("Brand not found, id=" + brandId);
        }
        return brand;
    }

    private Spu requireSpu(Long id) {
        Spu spu = spuMapper.selectById(id);
        if (spu == null) {
            throw new BizException("SPU not found, id=" + id);
        }
        return spu;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException("json serialize failed");
        }
    }

    private static class SpecDimension {
        private final String name;
        private final List<String> values;

        private SpecDimension(String name, List<String> values) {
            this.name = name;
            this.values = values;
        }
    }
}
