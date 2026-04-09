package com.demo.order.service;

import com.demo.order.dto.AddShipmentTraceRequest;
import com.demo.order.dto.ShipOrderRequest;
import com.demo.order.entity.Shipment;
import com.demo.order.entity.ShipmentTrace;
import java.util.List;

public interface LogisticsService {

    Shipment ship(ShipOrderRequest request);

    ShipmentTrace addTrace(AddShipmentTraceRequest request);

    void sign(Long orderId);

    List<ShipmentTrace> traces(Long orderId);
}
