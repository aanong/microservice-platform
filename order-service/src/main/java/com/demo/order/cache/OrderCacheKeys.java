package com.demo.order.cache;

public final class OrderCacheKeys {

    private OrderCacheKeys() {
    }

    public static String orderDetail(Long userId, Long orderId) {
        return "mall:order:detail:" + userId + ":" + orderId;
    }

    public static String orderOwner(Long orderId) {
        return "mall:order:owner:" + orderId;
    }

    public static String orderListVersion(Long userId) {
        return "mall:order:list:ver:" + userId;
    }

    public static String orderList(Long userId, String version) {
        return "mall:order:list:" + userId + ":v" + version;
    }

    public static String cartList(Long userId) {
        return "mall:order:cart:list:" + userId;
    }
}
