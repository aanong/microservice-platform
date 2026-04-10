package com.demo.stock.dto.spu;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class SpuUpdateRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotNull(message = "brandId is required")
    private Long brandId;

    private String mainImageUrl;
    private List<String> detailImages;
    private String description;

    @NotNull(message = "status is required")
    private Integer status;

    private List<SpuSpecRequest> specs;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
    public List<String> getDetailImages() { return detailImages; }
    public void setDetailImages(List<String> detailImages) { this.detailImages = detailImages; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<SpuSpecRequest> getSpecs() { return specs; }
    public void setSpecs(List<SpuSpecRequest> specs) { this.specs = specs; }
}
