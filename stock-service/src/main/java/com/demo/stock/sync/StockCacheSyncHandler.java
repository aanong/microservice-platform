package com.demo.stock.sync;

import com.demo.common.cache.CacheSyncEvent;
import com.demo.common.cache.CacheSyncEventUtils;
import com.demo.common.cache.CacheSyncHandler;
import com.demo.common.cache.RedisJsonCacheHelper;
import com.demo.stock.cache.StockCacheKeys;
import org.springframework.stereotype.Component;

/**
 * 库存服务缓存同步处理器。
 * <p>
 * 使用公共模块的 {@link RedisJsonCacheHelper} 和 {@link CacheSyncEventUtils}。
 */
@Component
public class StockCacheSyncHandler implements CacheSyncHandler {

    private static final String TABLE_PRODUCT = "mall_product";
    private static final String TABLE_CATEGORY = "mall_category";

    private final RedisJsonCacheHelper cacheHelper;

    public StockCacheSyncHandler(RedisJsonCacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    @Override
    public boolean supports(String table) {
        return TABLE_PRODUCT.equalsIgnoreCase(table) || TABLE_CATEGORY.equalsIgnoreCase(table);
    }

    @Override
    public void handle(CacheSyncEvent event) {
        if (TABLE_PRODUCT.equalsIgnoreCase(event.getTable())) {
            Long id = CacheSyncEventUtils.getLong(event, "id");
            if (id != null) {
                cacheHelper.delete(StockCacheKeys.productDetail(id));
            }
            cacheHelper.incrementVersion(StockCacheKeys.productListVersion());
            return;
        }

        if (TABLE_CATEGORY.equalsIgnoreCase(event.getTable())) {
            Long id = CacheSyncEventUtils.getLong(event, "id");
            if (id != null) {
                cacheHelper.delete(StockCacheKeys.categoryDetail(id));
            }
            cacheHelper.incrementVersion(StockCacheKeys.categoryListVersion());
            cacheHelper.incrementVersion(StockCacheKeys.productListVersion());
        }
    }
}
