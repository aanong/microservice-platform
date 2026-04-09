package com.demo.order.service.impl;

import com.demo.order.constant.BizConstants;
import com.demo.order.entity.OrderMain;
import com.demo.order.entity.OrderStatusLog;
import com.demo.order.entity.PaymentRecord;
import com.demo.order.exception.BizException;
import com.demo.order.mapper.OrderMainMapper;
import com.demo.order.mapper.OrderStatusLogMapper;
import com.demo.order.mapper.PaymentRecordMapper;
import com.demo.order.mq.OrderEventPublisher;
import com.demo.order.service.PaymentService;
import io.seata.spring.annotation.GlobalTransactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final OrderMainMapper orderMainMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final OrderEventPublisher orderEventPublisher;

    public PaymentServiceImpl(OrderMainMapper orderMainMapper,
                              PaymentRecordMapper paymentRecordMapper,
                              OrderStatusLogMapper orderStatusLogMapper,
                              OrderEventPublisher orderEventPublisher) {
        this.orderMainMapper = orderMainMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Override
    @GlobalTransactional(name = "order-simulate-pay", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public OrderMain simulatePay(Long userId, Long orderId) {
        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("Order not found");
        }
        if (!BizConstants.ORDER_STATUS_PENDING_PAY.equals(order.getOrderStatus())) {
            throw new BizException("Order is not pending payment");
        }

        PaymentRecord pay = new PaymentRecord();
        pay.setOrderId(order.getId());
        pay.setOrderNo(order.getOrderNo());
        pay.setPayNo("PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6));
        pay.setPayType("MOCK");
        pay.setPayStatus("SUCCESS");
        pay.setPayAmount(order.getPayAmount());
        pay.setPayTime(LocalDateTime.now());
        pay.setRemark("Simulated payment success");
        pay.setCreateTime(LocalDateTime.now());
        paymentRecordMapper.insert(pay);

        String oldStatus = order.getOrderStatus();
        order.setOrderStatus(BizConstants.ORDER_STATUS_PAID);
        order.setPaymentStatus(BizConstants.PAYMENT_STATUS_PAID);
        order.setPaidTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMainMapper.updateById(order);

        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setFromStatus(oldStatus);
        log.setToStatus(BizConstants.ORDER_STATUS_PAID);
        log.setRemark("Simulated payment completed");
        log.setCreateTime(LocalDateTime.now());
        orderStatusLogMapper.insert(log);

        orderEventPublisher.publishPaidEvent(order.getId(), order.getOrderNo(), order.getUserId());
        return order;
    }
}
