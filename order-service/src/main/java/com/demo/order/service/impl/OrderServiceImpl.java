package com.demo.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.common.entity.Product;
import com.demo.order.cache.OrderCacheKeys;
import com.demo.common.cache.RedisJsonCacheHelper;
import com.demo.order.constant.BizConstants;
import com.demo.order.dto.CreateOrderRequest;
import com.demo.order.dto.OrderDetailResponse;
import com.demo.order.entity.CartItem;
import com.demo.order.entity.CouponTemplate;
import com.demo.order.entity.CouponUser;
import com.demo.order.entity.OrderItem;
import com.demo.order.entity.OrderMain;
import com.demo.order.entity.OrderStatusLog;
import com.demo.order.entity.Shipment;
import com.demo.order.entity.ShipmentTrace;
import com.demo.order.exception.BizException;
import com.demo.order.mapper.CartItemMapper;
import com.demo.order.mapper.CouponTemplateMapper;
import com.demo.order.mapper.CouponUserMapper;
import com.demo.order.mapper.OrderItemMapper;
import com.demo.order.mapper.OrderMainMapper;
import com.demo.order.mapper.OrderStatusLogMapper;
import com.demo.order.mapper.ProductStockMapper;
import com.demo.order.mapper.ShipmentMapper;
import com.demo.order.mapper.ShipmentTraceMapper;
import com.demo.order.service.OrderService;
import com.fasterxml.jackson.core.type.TypeReference;
import io.seata.spring.annotation.GlobalTransactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final CartItemMapper cartItemMapper;
    private final CouponUserMapper couponUserMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final ProductStockMapper productStockMapper;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentTraceMapper shipmentTraceMapper;
    private final RedisJsonCacheHelper cacheHelper;

    public OrderServiceImpl(CartItemMapper cartItemMapper,
                            CouponUserMapper couponUserMapper,
                            CouponTemplateMapper couponTemplateMapper,
                            OrderMainMapper orderMainMapper,
                            OrderItemMapper orderItemMapper,
                            OrderStatusLogMapper orderStatusLogMapper,
                            ProductStockMapper productStockMapper,
                            ShipmentMapper shipmentMapper,
                            ShipmentTraceMapper shipmentTraceMapper,
                            RedisJsonCacheHelper cacheHelper) {
        this.cartItemMapper = cartItemMapper;
        this.couponUserMapper = couponUserMapper;
        this.couponTemplateMapper = couponTemplateMapper;
        this.orderMainMapper = orderMainMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
        this.productStockMapper = productStockMapper;
        this.shipmentMapper = shipmentMapper;
        this.shipmentTraceMapper = shipmentTraceMapper;
        this.cacheHelper = cacheHelper;
    }

    @Override
    @GlobalTransactional(name = "order-create", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public OrderMain create(CreateOrderRequest request) {
        List<CartItem> cartItems;
        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            cartItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, request.getUserId())
                .eq(CartItem::getChecked, 1));
        } else {
            cartItems = cartItemMapper.selectByUserAndIds(request.getUserId(), request.getCartItemIds());
        }
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BizException("No cart items selected");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Product product = productStockMapper.selectById(item.getProductId());
            if (product == null) {
                throw new BizException("Product not found: " + item.getProductId());
            }
            if (product.getStock() == null || product.getStock() < item.getQuantity()) {
                throw new BizException("Insufficient stock: " + item.getProductName());
            }
            item.setPrice(product.getPrice());
            item.setProductName(product.getName());
            item.setSkuCode(product.getSkuCode());
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        CouponUser couponUser = null;
        CouponTemplate couponTemplate = null;
        BigDecimal discount = BigDecimal.ZERO;

        if (request.getCouponUserId() != null) {
            couponUser = couponUserMapper.selectById(request.getCouponUserId());
            if (couponUser == null || !couponUser.getUserId().equals(request.getUserId())) {
                throw new BizException("Coupon not found");
            }
            if (!BizConstants.COUPON_STATUS_UNUSED.equals(couponUser.getStatus())) {
                throw new BizException("Coupon already used");
            }
            couponTemplate = couponTemplateMapper.selectById(couponUser.getTemplateId());
            if (couponTemplate == null) {
                throw new BizException("Coupon template missing");
            }
            discount = calculateDiscount(totalAmount, cartItems, couponTemplate);
        }

        BigDecimal payAmount = totalAmount.subtract(discount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        LocalDateTime now = LocalDateTime.now();
        String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        OrderMain order = new OrderMain();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setOrderStatus(BizConstants.ORDER_STATUS_PENDING_PAY);
        order.setPaymentStatus(BizConstants.PAYMENT_STATUS_UNPAID);
        order.setShippingStatus(BizConstants.SHIPPING_STATUS_TO_BE_SHIPPED);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discount);
        order.setPayAmount(payAmount);
        order.setCouponUserId(request.getCouponUserId());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        orderMainMapper.insert(order);

        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem cart = cartItems.get(i);
            BigDecimal lineTotal = cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            BigDecimal linePay;
            if (i == cartItems.size() - 1) {
                linePay = payAmount.subtract(allocated);
            } else {
                linePay = lineTotal.multiply(payAmount).divide(totalAmount, 2, RoundingMode.HALF_UP);
                allocated = allocated.add(linePay);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(cart.getProductId());
            orderItem.setProductName(cart.getProductName());
            orderItem.setSkuCode(cart.getSkuCode());
            orderItem.setSalePrice(cart.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalAmount(lineTotal);
            orderItem.setRealPayAmount(linePay);
            orderItem.setRefundedQuantity(0);
            orderItem.setRefundedAmount(BigDecimal.ZERO);
            orderItem.setCreateTime(now);
            orderItem.setUpdateTime(now);
            orderItemMapper.insert(orderItem);

            int updated = productStockMapper.deductStock(cart.getProductId(), cart.getQuantity());
            if (updated <= 0) {
                throw new BizException("Insufficient stock for product: " + cart.getProductId());
            }

            cartItemMapper.deleteById(cart.getId());
        }

        if (couponUser != null) {
            couponUser.setStatus(BizConstants.COUPON_STATUS_USED);
            couponUser.setUsedTime(now);
            couponUser.setUpdateTime(now);
            couponUserMapper.updateById(couponUser);
        }

        logStatus(order.getId(), orderNo, BizConstants.ORDER_STATUS_CREATED,
            BizConstants.ORDER_STATUS_PENDING_PAY, "Order created");
        return order;
    }

    @Override
    public OrderDetailResponse detail(Long orderId, Long userId) {
        String key = OrderCacheKeys.orderDetail(userId, orderId);
        OrderDetailResponse cached = cacheHelper.getObject(key, OrderDetailResponse.class);
        if (cached != null) {
            return cached;
        }

        OrderMain order = requireOrder(orderId, userId);
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        Shipment shipment = shipmentMapper.selectOne(new LambdaQueryWrapper<Shipment>()
            .eq(Shipment::getOrderId, orderId)
            .last("limit 1"));
        List<ShipmentTrace> traces = shipment == null
            ? Collections.<ShipmentTrace>emptyList()
            : shipmentTraceMapper.selectByShipmentId(shipment.getId());

        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrder(order);
        response.setItems(items);
        response.setShipment(shipment);
        response.setTraces(traces);

        cacheHelper.setDetail(key, response);
        cacheHelper.setString(OrderCacheKeys.orderOwner(orderId), String.valueOf(userId), Duration.ofHours(24));
        return response;
    }

    @Override
    public List<OrderMain> list(Long userId) {
        String version = cacheHelper.getVersion(OrderCacheKeys.orderListVersion(userId));
        String key = OrderCacheKeys.orderList(userId, version);

        List<OrderMain> cached = cacheHelper.getList(key, new TypeReference<List<OrderMain>>() {
        });
        if (cached != null) {
            return cached;
        }

        List<OrderMain> orders = orderMainMapper.selectList(new LambdaQueryWrapper<OrderMain>()
            .eq(OrderMain::getUserId, userId)
            .orderByDesc(OrderMain::getId));
        cacheHelper.setList(key, orders);
        return orders;
    }

    private OrderMain requireOrder(Long orderId, Long userId) {
        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("Order not found");
        }
        return order;
    }

    private BigDecimal calculateDiscount(BigDecimal total, List<CartItem> items, CouponTemplate template) {
        if (BizConstants.COUPON_TYPE_DIRECT_REDUCTION.equals(template.getType())) {
            return template.getDiscountAmount() == null ? BigDecimal.ZERO : template.getDiscountAmount().min(total);
        }

        if (BizConstants.COUPON_TYPE_FULL_REDUCTION.equals(template.getType())) {
            if (template.getThresholdAmount() == null || template.getDiscountAmount() == null) {
                throw new BizException("Invalid full reduction coupon");
            }
            if (total.compareTo(template.getThresholdAmount()) < 0) {
                throw new BizException("Order amount does not meet threshold");
            }
            return template.getDiscountAmount().min(total);
        }

        if (BizConstants.COUPON_TYPE_FLASH_SALE.equals(template.getType())) {
            if (template.getSeckillProductId() == null || template.getSeckillPrice() == null) {
                throw new BizException("Invalid flash sale coupon");
            }
            BigDecimal discount = BigDecimal.ZERO;
            for (CartItem item : items) {
                if (template.getSeckillProductId().equals(item.getProductId())
                    && item.getPrice().compareTo(template.getSeckillPrice()) > 0) {
                    BigDecimal line = item.getPrice().subtract(template.getSeckillPrice())
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                    discount = discount.add(line);
                }
            }
            if (discount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("No matching flash sale product in order");
            }
            return discount.min(total);
        }

        throw new BizException("Unsupported coupon type: " + template.getType());
    }

    private void logStatus(Long orderId, String orderNo, String fromStatus, String toStatus, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        orderStatusLogMapper.insert(log);
    }
}
