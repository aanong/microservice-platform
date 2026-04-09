package com.demo.order.service.impl;

import com.demo.order.constant.BizConstants;
import com.demo.order.dto.RefundRequest;
import com.demo.order.entity.OrderItem;
import com.demo.order.entity.OrderMain;
import com.demo.order.entity.OrderStatusLog;
import com.demo.order.entity.PaymentRecord;
import com.demo.order.entity.RefundItem;
import com.demo.order.entity.RefundMain;
import com.demo.order.exception.BizException;
import com.demo.order.mapper.OrderItemMapper;
import com.demo.order.mapper.OrderMainMapper;
import com.demo.order.mapper.OrderStatusLogMapper;
import com.demo.order.mapper.PaymentRecordMapper;
import com.demo.order.mapper.ProductStockMapper;
import com.demo.order.mapper.RefundItemMapper;
import com.demo.order.mapper.RefundMainMapper;
import com.demo.order.service.RefundService;
import io.seata.spring.annotation.GlobalTransactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundServiceImpl implements RefundService {

    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final RefundMainMapper refundMainMapper;
    private final RefundItemMapper refundItemMapper;
    private final ProductStockMapper productStockMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;

    public RefundServiceImpl(OrderMainMapper orderMainMapper,
                             OrderItemMapper orderItemMapper,
                             RefundMainMapper refundMainMapper,
                             RefundItemMapper refundItemMapper,
                             ProductStockMapper productStockMapper,
                             PaymentRecordMapper paymentRecordMapper,
                             OrderStatusLogMapper orderStatusLogMapper) {
        this.orderMainMapper = orderMainMapper;
        this.orderItemMapper = orderItemMapper;
        this.refundMainMapper = refundMainMapper;
        this.refundItemMapper = refundItemMapper;
        this.productStockMapper = productStockMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
    }

    @Override
    @GlobalTransactional(name = "order-refund", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public RefundMain refund(RefundRequest request) {
        OrderMain order = orderMainMapper.selectById(request.getOrderId());
        if (order == null || !order.getUserId().equals(request.getUserId())) {
            throw new BizException("Order not found");
        }
        if (!BizConstants.PAYMENT_STATUS_PAID.equals(order.getPaymentStatus())
            && !BizConstants.PAYMENT_STATUS_PARTIAL_REFUNDED.equals(order.getPaymentStatus())) {
            throw new BizException("Current order cannot be refunded");
        }

        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
        if (orderItems.isEmpty()) {
            throw new BizException("No order items");
        }

        List<OrderItem> targetItems = new ArrayList<OrderItem>();
        boolean fullRefund = request.getOrderItemId() == null;
        if (fullRefund) {
            targetItems.addAll(orderItems);
        } else {
            for (OrderItem item : orderItems) {
                if (item.getId().equals(request.getOrderItemId())) {
                    targetItems.add(item);
                    break;
                }
            }
            if (targetItems.isEmpty()) {
                throw new BizException("Order item not found");
            }
        }

        RefundMain refundMain = new RefundMain();
        refundMain.setRefundNo("REF" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6));
        refundMain.setOrderId(order.getId());
        refundMain.setOrderNo(order.getOrderNo());
        refundMain.setUserId(order.getUserId());
        refundMain.setRefundType(fullRefund ? "FULL" : "ITEM");
        refundMain.setRefundStatus(BizConstants.REFUND_STATUS_SUCCESS);
        refundMain.setRefundAmount(BigDecimal.ZERO);
        refundMain.setReason(request.getReason());
        refundMain.setCreateTime(LocalDateTime.now());
        refundMain.setUpdateTime(LocalDateTime.now());
        refundMainMapper.insert(refundMain);

        BigDecimal refundAmount = BigDecimal.ZERO;
        for (OrderItem item : targetItems) {
            int availableQty = item.getQuantity() - (item.getRefundedQuantity() == null ? 0 : item.getRefundedQuantity());
            if (availableQty <= 0) {
                continue;
            }
            int refundQty = fullRefund ? availableQty : (request.getQuantity() == null ? 0 : request.getQuantity());
            if (refundQty <= 0 || refundQty > availableQty) {
                throw new BizException("Invalid refund quantity");
            }

            BigDecimal unitPay = item.getRealPayAmount().divide(BigDecimal.valueOf(item.getQuantity()), 6, RoundingMode.HALF_UP);
            BigDecimal lineRefund = unitPay.multiply(BigDecimal.valueOf(refundQty)).setScale(2, RoundingMode.HALF_UP);

            RefundItem refundItem = new RefundItem();
            refundItem.setRefundId(refundMain.getId());
            refundItem.setOrderItemId(item.getId());
            refundItem.setQuantity(refundQty);
            refundItem.setRefundAmount(lineRefund);
            refundItem.setCreateTime(LocalDateTime.now());
            refundItemMapper.insert(refundItem);

            item.setRefundedQuantity((item.getRefundedQuantity() == null ? 0 : item.getRefundedQuantity()) + refundQty);
            item.setRefundedAmount((item.getRefundedAmount() == null ? BigDecimal.ZERO : item.getRefundedAmount()).add(lineRefund));
            item.setUpdateTime(LocalDateTime.now());
            orderItemMapper.updateById(item);

            productStockMapper.addStock(item.getProductId(), refundQty);
            refundAmount = refundAmount.add(lineRefund);

            if (!fullRefund) {
                break;
            }
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("No refundable amount");
        }

        refundMain.setRefundAmount(refundAmount);
        refundMain.setUpdateTime(LocalDateTime.now());
        refundMainMapper.updateById(refundMain);

        PaymentRecord refundPay = new PaymentRecord();
        refundPay.setOrderId(order.getId());
        refundPay.setOrderNo(order.getOrderNo());
        refundPay.setPayNo(refundMain.getRefundNo());
        refundPay.setPayType("MOCK_REFUND");
        refundPay.setPayStatus("SUCCESS");
        refundPay.setPayAmount(refundAmount.negate());
        refundPay.setPayTime(LocalDateTime.now());
        refundPay.setRemark("Simulated refund success");
        refundPay.setCreateTime(LocalDateTime.now());
        paymentRecordMapper.insert(refundPay);

        boolean allRefunded = true;
        List<OrderItem> latest = orderItemMapper.selectByOrderId(order.getId());
        for (OrderItem item : latest) {
            if ((item.getRefundedQuantity() == null ? 0 : item.getRefundedQuantity()) < item.getQuantity()) {
                allRefunded = false;
                break;
            }
        }

        String fromStatus = order.getOrderStatus();
        if (allRefunded) {
            order.setOrderStatus(BizConstants.ORDER_STATUS_REFUNDED);
            order.setPaymentStatus(BizConstants.PAYMENT_STATUS_REFUNDED);
        } else {
            order.setOrderStatus(BizConstants.ORDER_STATUS_PARTIALLY_REFUNDED);
            order.setPaymentStatus(BizConstants.PAYMENT_STATUS_PARTIAL_REFUNDED);
        }
        order.setUpdateTime(LocalDateTime.now());
        orderMainMapper.updateById(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setFromStatus(fromStatus);
        log.setToStatus(order.getOrderStatus());
        log.setRemark("Refund completed");
        log.setCreateTime(LocalDateTime.now());
        orderStatusLogMapper.insert(log);

        return refundMain;
    }
}
