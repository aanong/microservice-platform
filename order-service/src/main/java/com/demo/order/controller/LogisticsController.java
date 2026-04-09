package com.demo.order.controller;

import com.demo.common.api.ApiResponse;
import com.demo.order.dto.AddShipmentTraceRequest;
import com.demo.order.dto.ShipOrderRequest;
import com.demo.order.entity.Shipment;
import com.demo.order.entity.ShipmentTrace;
import com.demo.order.service.LogisticsService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    private final LogisticsService logisticsService;

    public LogisticsController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    @PostMapping("/ship")
    public ApiResponse<Shipment> ship(@Valid @RequestBody ShipOrderRequest request) {
        return ApiResponse.ok("Shipped", logisticsService.ship(request));
    }

    @PostMapping("/trace")
    public ApiResponse<ShipmentTrace> addTrace(@Valid @RequestBody AddShipmentTraceRequest request) {
        return ApiResponse.ok("Trace added", logisticsService.addTrace(request));
    }

    @PostMapping("/sign/{orderId}")
    public ApiResponse<Void> sign(@PathVariable Long orderId) {
        logisticsService.sign(orderId);
        return ApiResponse.ok("Signed", null);
    }

    @GetMapping("/trace")
    public ApiResponse<List<ShipmentTrace>> traces(@RequestParam Long orderId) {
        return ApiResponse.ok(logisticsService.traces(orderId));
    }
}
