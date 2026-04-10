package com.demo.stock.dto.sku;

import javax.validation.constraints.NotNull;

public class SkuUpdateStatusRequest {

    @NotNull(message = "status is required")
    private Integer status;

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
