package com.demo.common.cache;

import java.util.Map;

/**
 * CacheSyncEvent 数据提取工具类。
 * <p>
 * 从各服务的 CacheSyncHandler 实现中提取的公共逻辑，
 * 支持从 event 的 after/before/pk 中按优先级提取字段值。
 */
public final class CacheSyncEventUtils {

    private CacheSyncEventUtils() {
    }

    /**
     * 从事件中提取 Long 类型的字段值。
     * 查找优先级：after → before → pk
     */
    public static Long getLong(CacheSyncEvent event, String key) {
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

    /**
     * 从事件中提取 String 类型的字段值。
     * 查找优先级：after → before → pk
     */
    public static String getString(CacheSyncEvent event, String key) {
        Object value = pick(event.getAfter(), key);
        if (value == null) {
            value = pick(event.getBefore(), key);
        }
        if (value == null && event.getPk() != null) {
            value = event.getPk().get(key);
        }
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 从 Map 中安全取值
     */
    public static Object pick(Map<String, Object> data, String key) {
        if (data == null) {
            return null;
        }
        return data.get(key);
    }
}
