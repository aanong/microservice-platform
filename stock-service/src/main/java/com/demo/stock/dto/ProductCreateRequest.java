package com.demo.stock.dto;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ProductCreateRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "skuCode is required")
    private String skuCode;

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.00", message = "price must be >= 0")
    private BigDecimal price;

    @NotNull(message = "stock is required")
    private Integer stock;

    @NotNull(message = "status is required")
    private Integer status;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
