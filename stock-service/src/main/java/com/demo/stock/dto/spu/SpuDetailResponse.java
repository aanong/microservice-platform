package com.demo.stock.dto.spu;

import com.demo.common.entity.Sku;
import com.demo.common.entity.Spu;
import java.util.List;

public class SpuDetailResponse {

    private Spu spu;
    private List<SpuSpecItem> specs;
    private List<Sku> skus;

    public Spu getSpu() { return spu; }
    public void setSpu(Spu spu) { this.spu = spu; }
    public List<SpuSpecItem> getSpecs() { return specs; }
    public void setSpecs(List<SpuSpecItem> specs) { this.specs = specs; }
    public List<Sku> getSkus() { return skus; }
    public void setSkus(List<Sku> skus) { this.skus = skus; }
}
