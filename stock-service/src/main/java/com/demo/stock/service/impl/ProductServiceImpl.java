package com.demo.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.common.cache.RedisJsonCacheHelper;
import com.demo.stock.cache.StockCacheKeys;
import com.demo.stock.dto.ProductCreateRequest;
import com.demo.stock.dto.ProductUpdateRequest;
import com.demo.common.entity.Category;
import com.demo.common.entity.Product;
import com.demo.stock.exception.BizException;
import com.demo.stock.mapper.CategoryMapper;
import com.demo.stock.mapper.ProductMapper;
import com.demo.stock.mq.ProductEventPublisher;
import com.demo.stock.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import io.seata.spring.annotation.GlobalTransactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ProductEventPublisher productEventPublisher;
    private final RedisJsonCacheHelper cacheHelper;

    public ProductServiceImpl(ProductMapper productMapper,
            CategoryMapper categoryMapper,
            ProductEventPublisher productEventPublisher,
            RedisJsonCacheHelper cacheHelper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.productEventPublisher = productEventPublisher;
        this.cacheHelper = cacheHelper;
    }

    @Override
    @GlobalTransactional(name = "stock-create-product", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Product create(ProductCreateRequest request) {
        requireCategory(request.getCategoryId());
        checkSkuExists(request.getSkuCode(), null);

        Product product = new Product();
        product.setName(request.getName());
        product.setSkuCode(request.getSkuCode());
        product.setCategoryId(request.getCategoryId());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setDescription(request.getDescription());
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());

        productMapper.insert(product);
        productEventPublisher.publish("CREATE", product);
        return product;
    }

    @Override
    @GlobalTransactional(name = "stock-update-product", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Product update(Long id, ProductUpdateRequest request) {
        Product exists = requireProduct(id);
        requireCategory(request.getCategoryId());
        checkSkuExists(request.getSkuCode(), id);

        exists.setName(request.getName());
        exists.setSkuCode(request.getSkuCode());
        exists.setCategoryId(request.getCategoryId());
        exists.setPrice(request.getPrice());
        exists.setStock(request.getStock());
        exists.setStatus(request.getStatus());
        exists.setDescription(request.getDescription());
        exists.setUpdateTime(LocalDateTime.now());

        productMapper.updateById(exists);
        productEventPublisher.publish("UPDATE", exists);
        return exists;
    }

    @Override
    @GlobalTransactional(name = "stock-delete-product", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Product exists = requireProduct(id);
        productMapper.deleteById(id);
        productEventPublisher.publish("DELETE", exists);
    }

    @Override
    public Product detail(Long id) {
        String key = StockCacheKeys.productDetail(id);
        Product cached = cacheHelper.getObject(key, Product.class);
        if (cached != null) {
            return cached;
        }

        Product product = productMapper.selectById(id);
        if (product == null) {
            cacheHelper.setNull(key);
            throw new BizException("Product not found, id=" + id);
        }
        cacheHelper.setDetail(key, product);
        return product;
    }

    @Override
    public List<Product> list(String keyword, Long categoryId) {
        String version = cacheHelper.getVersion(StockCacheKeys.productListVersion());
        String key = StockCacheKeys.productList(keyword, categoryId, version);

        List<Product> cached = cacheHelper.getList(key, new TypeReference<List<Product>>() {
        });
        if (cached != null) {
            return cached;
        }

        List<Product> products = productMapper.selectByCondition(keyword, categoryId);
        cacheHelper.setList(key, products);
        return products;
    }

    private Category requireCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BizException("Category not found, id=" + categoryId);
        }
        return category;
    }

    private Product requireProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException("Product not found, id=" + id);
        }
        return product;
    }

    private void checkSkuExists(String skuCode, Long excludeId) {
        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<Product>()
                .eq(Product::getSkuCode, skuCode);
        if (excludeId != null) {
            query.ne(Product::getId, excludeId);
        }
        Long count = productMapper.selectCount(query);
        if (count != null && count > 0L) {
            throw new BizException("skuCode already exists: " + skuCode);
        }
    }
}
