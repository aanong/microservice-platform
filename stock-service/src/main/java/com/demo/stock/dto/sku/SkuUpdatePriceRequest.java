package com.demo.stock.dto.sku;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

public class SkuUpdatePriceRequest {

    @NotNull(message = "salePrice is required")
    @DecimalMin(value = "0.00", message = "salePrice must be >= 0")
    private BigDecimal salePrice;

    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
}
