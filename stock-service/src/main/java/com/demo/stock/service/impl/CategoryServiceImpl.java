package com.demo.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.stock.cache.RedisJsonCacheHelper;
import com.demo.stock.cache.StockCacheKeys;
import com.demo.stock.dto.CategoryCreateRequest;
import com.demo.stock.dto.CategoryUpdateRequest;
import com.demo.common.entity.Category;
import com.demo.common.entity.Product;
import com.demo.stock.exception.BizException;
import com.demo.stock.mapper.CategoryMapper;
import com.demo.stock.mapper.ProductMapper;
import com.demo.stock.service.CategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final RedisJsonCacheHelper cacheHelper;

    public CategoryServiceImpl(CategoryMapper categoryMapper,
                               ProductMapper productMapper,
                               RedisJsonCacheHelper cacheHelper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.cacheHelper = cacheHelper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category create(CategoryCreateRequest request) {
        checkCodeExists(request.getCode(), null);
        Category category = new Category();
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setSort(request.getSort());
        category.setStatus(request.getStatus());
        category.setRemark(request.getRemark());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.insert(category);
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category update(Long id, CategoryUpdateRequest request) {
        Category exists = requireCategory(id);
        checkCodeExists(request.getCode(), id);
        exists.setName(request.getName());
        exists.setCode(request.getCode());
        exists.setSort(request.getSort());
        exists.setStatus(request.getStatus());
        exists.setRemark(request.getRemark());
        exists.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(exists);
        return exists;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireCategory(id);
        Long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
            .eq(Product::getCategoryId, id));
        if (productCount != null && productCount > 0L) {
            throw new BizException("Category has products and cannot be deleted");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public Category detail(Long id) {
        String key = StockCacheKeys.categoryDetail(id);
        Category cached = cacheHelper.getObject(key, Category.class);
        if (cached != null) {
            return cached;
        }

        Category category = categoryMapper.selectById(id);
        if (category == null) {
            cacheHelper.setNull(key);
            throw new BizException("Category not found, id=" + id);
        }
        cacheHelper.setDetail(key, category);
        return category;
    }

    @Override
    public List<Category> list(String keyword) {
        String version = cacheHelper.getVersion(StockCacheKeys.categoryListVersion());
        String key = StockCacheKeys.categoryList(keyword, version);

        List<Category> cached = cacheHelper.getList(key, new TypeReference<List<Category>>() {
        });
        if (cached != null) {
            return cached;
        }

        List<Category> categories = categoryMapper.selectByKeyword(keyword);
        cacheHelper.setList(key, categories);
        return categories;
    }

    private Category requireCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException("Category not found, id=" + id);
        }
        return category;
    }

    private void checkCodeExists(String code, Long excludeId) {
        LambdaQueryWrapper<Category> query = new LambdaQueryWrapper<Category>()
            .eq(Category::getCode, code);
        if (excludeId != null) {
            query.ne(Category::getId, excludeId);
        }
        Long count = categoryMapper.selectCount(query);
        if (count != null && count > 0L) {
            throw new BizException("Category code already exists: " + code);
        }
    }
}
