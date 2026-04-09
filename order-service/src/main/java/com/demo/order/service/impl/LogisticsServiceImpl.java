package com.demo.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.order.constant.BizConstants;
import com.demo.order.dto.AddShipmentTraceRequest;
import com.demo.order.dto.ShipOrderRequest;
import com.demo.order.entity.OrderMain;
import com.demo.order.entity.OrderStatusLog;
import com.demo.order.entity.Shipment;
import com.demo.order.entity.ShipmentTrace;
import com.demo.order.exception.BizException;
import com.demo.order.mapper.OrderMainMapper;
import com.demo.order.mapper.OrderStatusLogMapper;
import com.demo.order.mapper.ShipmentMapper;
import com.demo.order.mapper.ShipmentTraceMapper;
import com.demo.order.service.LogisticsService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogisticsServiceImpl implements LogisticsService {

    private final OrderMainMapper orderMainMapper;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentTraceMapper shipmentTraceMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;

    public LogisticsServiceImpl(OrderMainMapper orderMainMapper,
                                ShipmentMapper shipmentMapper,
                                ShipmentTraceMapper shipmentTraceMapper,
                                OrderStatusLogMapper orderStatusLogMapper) {
        this.orderMainMapper = orderMainMapper;
        this.shipmentMapper = shipmentMapper;
        this.shipmentTraceMapper = shipmentTraceMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Shipment ship(ShipOrderRequest request) {
        OrderMain order = orderMainMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BizException("Order not found");
        }
        if (!BizConstants.ORDER_STATUS_PAID.equals(order.getOrderStatus())) {
            throw new BizException("Only paid orders can be shipped");
        }

        Shipment shipment = new Shipment();
        shipment.setOrderId(order.getId());
        shipment.setOrderNo(order.getOrderNo());
        shipment.setCarrierCode(request.getCarrierCode());
        shipment.setTrackingNo(request.getTrackingNo());
        shipment.setShipmentStatus(BizConstants.SHIPPING_STATUS_SHIPPED);
        shipment.setShippedTime(LocalDateTime.now());
        shipment.setCreateTime(LocalDateTime.now());
        shipment.setUpdateTime(LocalDateTime.now());
        shipmentMapper.insert(shipment);

        ShipmentTrace trace = new ShipmentTrace();
        trace.setShipmentId(shipment.getId());
        trace.setTraceStatus(BizConstants.SHIPPING_STATUS_SHIPPED);
        trace.setContent("Package shipped");
        trace.setTraceTime(LocalDateTime.now());
        trace.setCreateTime(LocalDateTime.now());
        shipmentTraceMapper.insert(trace);

        String fromStatus = order.getOrderStatus();
        order.setOrderStatus(BizConstants.ORDER_STATUS_SHIPPING);
        order.setShippingStatus(BizConstants.SHIPPING_STATUS_SHIPPED);
        order.setUpdateTime(LocalDateTime.now());
        orderMainMapper.updateById(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setFromStatus(fromStatus);
        log.setToStatus(BizConstants.ORDER_STATUS_SHIPPING);
        log.setRemark("Order shipped");
        log.setCreateTime(LocalDateTime.now());
        orderStatusLogMapper.insert(log);
        return shipment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShipmentTrace addTrace(AddShipmentTraceRequest request) {
        Shipment shipment = shipmentMapper.selectById(request.getShipmentId());
        if (shipment == null) {
            throw new BizException("Shipment not found");
        }
        ShipmentTrace trace = new ShipmentTrace();
        trace.setShipmentId(shipment.getId());
        trace.setTraceStatus(request.getTraceStatus());
        trace.setContent(request.getContent());
        trace.setTraceTime(LocalDateTime.now());
        trace.setCreateTime(LocalDateTime.now());
        shipmentTraceMapper.insert(trace);

        shipment.setShipmentStatus(request.getTraceStatus());
        shipment.setUpdateTime(LocalDateTime.now());
        shipmentMapper.updateById(shipment);
        return trace;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sign(Long orderId) {
        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("Order not found");
        }
        Shipment shipment = shipmentMapper.selectOne(new LambdaQueryWrapper<Shipment>()
            .eq(Shipment::getOrderId, orderId)
            .last("limit 1"));
        if (shipment == null) {
            throw new BizException("Shipment not found");
        }

        ShipmentTrace trace = new ShipmentTrace();
        trace.setShipmentId(shipment.getId());
        trace.setTraceStatus(BizConstants.SHIPPING_STATUS_SIGNED);
        trace.setContent("Order signed");
        trace.setTraceTime(LocalDateTime.now());
        trace.setCreateTime(LocalDateTime.now());
        shipmentTraceMapper.insert(trace);

        shipment.setShipmentStatus(BizConstants.SHIPPING_STATUS_SIGNED);
        shipment.setSignedTime(LocalDateTime.now());
        shipment.setUpdateTime(LocalDateTime.now());
        shipmentMapper.updateById(shipment);

        String fromStatus = order.getOrderStatus();
        order.setOrderStatus(BizConstants.ORDER_STATUS_COMPLETED);
        order.setShippingStatus(BizConstants.SHIPPING_STATUS_SIGNED);
        order.setUpdateTime(LocalDateTime.now());
        orderMainMapper.updateById(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setFromStatus(fromStatus);
        log.setToStatus(BizConstants.ORDER_STATUS_COMPLETED);
        log.setRemark("Order signed and completed");
        log.setCreateTime(LocalDateTime.now());
        orderStatusLogMapper.insert(log);
    }

    @Override
    public List<ShipmentTrace> traces(Long orderId) {
        Shipment shipment = shipmentMapper.selectOne(new LambdaQueryWrapper<Shipment>()
            .eq(Shipment::getOrderId, orderId)
            .last("limit 1"));
        if (shipment == null) {
            throw new BizException("Shipment not found");
        }
        return shipmentTraceMapper.selectByShipmentId(shipment.getId());
    }
}
