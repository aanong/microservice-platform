package com.demo.stock.dto.spu;

import java.util.List;
import javax.validation.constraints.NotBlank;

public class SpuSpecRequest {

    @NotBlank(message = "specName is required")
    private String specName;
    private Integer sort;
    private List<String> values;

    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public List<String> getValues() { return values; }
    public void setValues(List<String> values) { this.values = values; }
}
