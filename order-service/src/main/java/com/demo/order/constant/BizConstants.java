package com.demo.order.constant;

public final class BizConstants {

    private BizConstants() {
    }

    public static final String ORDER_STATUS_CREATED = "CREATED";
    public static final String ORDER_STATUS_PENDING_PAY = "PENDING_PAY";
    public static final String ORDER_STATUS_PAID = "PAID";
    public static final String ORDER_STATUS_SHIPPING = "SHIPPING";
    public static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    public static final String ORDER_STATUS_PARTIALLY_REFUNDED = "PARTIALLY_REFUNDED";
    public static final String ORDER_STATUS_REFUNDED = "REFUNDED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    public static final String PAYMENT_STATUS_UNPAID = "UNPAID";
    public static final String PAYMENT_STATUS_PAID = "PAID";
    public static final String PAYMENT_STATUS_PARTIAL_REFUNDED = "PARTIAL_REFUNDED";
    public static final String PAYMENT_STATUS_REFUNDED = "REFUNDED";

    public static final String SHIPPING_STATUS_TO_BE_SHIPPED = "TO_BE_SHIPPED";
    public static final String SHIPPING_STATUS_SHIPPED = "SHIPPED";
    public static final String SHIPPING_STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String SHIPPING_STATUS_SIGNED = "SIGNED";

    public static final String COUPON_TYPE_FULL_REDUCTION = "FULL_REDUCTION";
    public static final String COUPON_TYPE_FLASH_SALE = "FLASH_SALE";
    public static final String COUPON_TYPE_DIRECT_REDUCTION = "DIRECT_REDUCTION";

    public static final String COUPON_STATUS_UNUSED = "UNUSED";
    public static final String COUPON_STATUS_USED = "USED";

    public static final String PROMOTION_TYPE_FULL_REDUCTION = "FULL_REDUCTION";
    public static final String PROMOTION_TYPE_FLASH_SALE = "FLASH_SALE";
    public static final String PROMOTION_TYPE_DIRECT_REDUCTION = "DIRECT_REDUCTION";

    public static final String REFUND_STATUS_SUCCESS = "REFUND_SUCCESS";
}
