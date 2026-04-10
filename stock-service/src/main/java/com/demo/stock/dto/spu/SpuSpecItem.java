package com.demo.stock.dto.spu;

import java.util.List;

public class SpuSpecItem {

    private String specName;
    private List<String> values;

    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public List<String> getValues() { return values; }
    public void setValues(List<String> values) { this.values = values; }
}
