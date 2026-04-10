package com.demo.stock.dto.sku;

import javax.validation.constraints.NotNull;

public class SkuUpdateStockRequest {

    @NotNull(message = "stock is required")
    private Integer stock;

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
