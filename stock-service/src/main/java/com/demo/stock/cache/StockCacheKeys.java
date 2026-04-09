package com.demo.stock.cache;

public final class StockCacheKeys {

    private StockCacheKeys() {
    }

    public static String productDetail(Long id) {
        return "mall:stock:product:" + id;
    }

    public static String productListVersion() {
        return "mall:stock:product:list:ver";
    }

    public static String productList(String keyword, Long categoryId, String version) {
        return "mall:stock:product:list:" + safe(keyword) + ":" + safe(categoryId) + ":v" + version;
    }

    public static String categoryDetail(Long id) {
        return "mall:stock:category:" + id;
    }

    public static String categoryListVersion() {
        return "mall:stock:category:list:ver";
    }

    public static String categoryList(String keyword, String version) {
        return "mall:stock:category:list:" + safe(keyword) + ":v" + version;
    }

    private static String safe(Object value) {
        return value == null ? "_" : String.valueOf(value);
    }
}
