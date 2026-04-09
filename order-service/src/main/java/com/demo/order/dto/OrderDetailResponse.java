package com.demo.order.dto;

import com.demo.order.entity.OrderItem;
import com.demo.order.entity.OrderMain;
import com.demo.order.entity.Shipment;
import com.demo.order.entity.ShipmentTrace;
import java.util.List;
import lombok.Data;

@Data
public class OrderDetailResponse {

    private OrderMain order;
    private List<OrderItem> items;
    private Shipment shipment;
    private List<ShipmentTrace> traces;
}
