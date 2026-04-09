package com.demo.order.sync;

import com.demo.common.cache.CacheSyncEvent;
import com.demo.common.cache.CacheSyncHandler;
import com.demo.order.cache.OrderCacheKeys;
import com.demo.order.cache.RedisJsonCacheHelper;
import com.demo.order.entity.OrderMain;
import com.demo.order.mapper.OrderMainMapper;
import java.time.Duration;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OrderCacheSyncHandler implements CacheSyncHandler {

    private final RedisJsonCacheHelper cacheHelper;
    private final OrderMainMapper orderMainMapper;

    public OrderCacheSyncHandler(RedisJsonCacheHelper cacheHelper, OrderMainMapper orderMainMapper) {
        this.cacheHelper = cacheHelper;
        this.orderMainMapper = orderMainMapper;
    }

    @Override
    public boolean supports(String table) {
        if (table == null) {
            return false;
        }
        String t = table.toLowerCase();
        return "order_main".equals(t)
            || "order_item".equals(t)
            || "shipment".equals(t)
            || "shipment_trace".equals(t)
            || "payment_record".equals(t)
            || "order_status_log".equals(t)
            || "refund_main".equals(t)
            || "refund_item".equals(t)
            || "cart_item".equals(t)
            || "coupon_user".equals(t)
            || "coupon_template".equals(t);
    }

    @Override
    public void handle(CacheSyncEvent event) {
        String table = event.getTable().toLowerCase();
        if ("cart_item".equals(table)) {
            Long userId = getLong(event, "user_id");
            if (userId != null) {
                cacheHelper.delete(OrderCacheKeys.cartList(userId));
            }
            return;
        }

        if ("order_main".equals(table)) {
            Long orderId = getLong(event, "id");
            Long userId = getLong(event, "user_id");
            invalidateOrder(orderId, userId);
            return;
        }

        Long orderId = getLong(event, "order_id");
        invalidateOrder(orderId, null);
    }

    private void invalidateOrder(Long orderId, Long userId) {
        if (orderId == null) {
            return;
        }
        Long resolvedUserId = userId == null ? resolveUserIdByOrder(orderId) : userId;
        if (resolvedUserId != null) {
            cacheHelper.delete(OrderCacheKeys.orderDetail(resolvedUserId, orderId));
            cacheHelper.incrementVersion(OrderCacheKeys.orderListVersion(resolvedUserId));
            cacheHelper.setString(OrderCacheKeys.orderOwner(orderId), String.valueOf(resolvedUserId), Duration.ofHours(24));
        }
    }

    private Long resolveUserIdByOrder(Long orderId) {
        String userIdValue = cacheHelper.getString(OrderCacheKeys.orderOwner(orderId));
        if (userIdValue != null && !userIdValue.trim().isEmpty()) {
            return Long.valueOf(userIdValue);
        }
        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null || order.getUserId() == null) {
            return null;
        }
        cacheHelper.setString(OrderCacheKeys.orderOwner(orderId), String.valueOf(order.getUserId()), Duration.ofHours(24));
        return order.getUserId();
    }

    private Long getLong(CacheSyncEvent event, String key) {
        Object value = pick(event.getAfter(), key);
        if (value == null) {
            value = pick(event.getBefore(), key);
        }
        if (value == null && event.getPk() != null) {
            value = event.getPk().get(key);
        }
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Object pick(Map<String, Object> data, String key) {
        if (data == null) {
            return null;
        }
        return data.get(key);
    }
}
