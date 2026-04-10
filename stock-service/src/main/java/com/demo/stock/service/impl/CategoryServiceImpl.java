package com.demo.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.common.cache.RedisJsonCacheHelper;
import com.demo.common.entity.Category;
import com.demo.common.entity.Spu;
import com.demo.stock.cache.StockCacheKeys;
import com.demo.stock.dto.CategoryCreateRequest;
import com.demo.stock.dto.CategoryTreeNode;
import com.demo.stock.dto.CategoryUpdateRequest;
import com.demo.stock.exception.BizException;
import com.demo.stock.mapper.CategoryMapper;
import com.demo.stock.mapper.SpuMapper;
import com.demo.stock.service.CategoryService;
import com.demo.stock.service.ProductSearchService;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final SpuMapper spuMapper;
    private final RedisJsonCacheHelper cacheHelper;
    private final ProductSearchService productSearchService;

    public CategoryServiceImpl(CategoryMapper categoryMapper,
                               SpuMapper spuMapper,
                               RedisJsonCacheHelper cacheHelper,
                               ProductSearchService productSearchService) {
        this.categoryMapper = categoryMapper;
        this.spuMapper = spuMapper;
        this.cacheHelper = cacheHelper;
        this.productSearchService = productSearchService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category create(CategoryCreateRequest request) {
        checkCodeExists(request.getCode(), null);
        Long parentId = request.getParentId() == null ? 0L : request.getParentId();
        int level = calcLevel(parentId, null);

        Category category = new Category();
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setParentId(parentId);
        category.setLevel(level);
        category.setImageUrl(request.getImageUrl());
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
        Long parentId = request.getParentId() == null ? 0L : request.getParentId();
        int level = calcLevel(parentId, id);

        exists.setName(request.getName());
        exists.setCode(request.getCode());
        exists.setParentId(parentId);
        exists.setLevel(level);
        exists.setImageUrl(request.getImageUrl());
        exists.setSort(request.getSort());
        exists.setStatus(request.getStatus());
        exists.setRemark(request.getRemark());
        exists.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(exists);
        productSearchService.rebuildByCategory(id);
        return exists;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireCategory(id);

        Long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
            .eq(Category::getParentId, id));
        if (childCount != null && childCount > 0L) {
            throw new BizException("Category has children and cannot be deleted");
        }

        Long spuCount = spuMapper.selectCount(new LambdaQueryWrapper<Spu>()
            .eq(Spu::getCategoryId, id));
        if (spuCount != null && spuCount > 0L) {
            throw new BizException("Category has SPU and cannot be deleted");
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

    @Override
    public List<CategoryTreeNode> tree() {
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
            .orderByAsc(Category::getSort)
            .orderByAsc(Category::getId));

        Map<Long, CategoryTreeNode> nodeMap = new HashMap<Long, CategoryTreeNode>();
        List<CategoryTreeNode> roots = new ArrayList<CategoryTreeNode>();

        for (Category category : categories) {
            CategoryTreeNode node = toNode(category);
            nodeMap.put(category.getId(), node);
        }

        for (Category category : categories) {
            CategoryTreeNode node = nodeMap.get(category.getId());
            Long pid = category.getParentId() == null ? 0L : category.getParentId();
            if (pid == 0L) {
                roots.add(node);
                continue;
            }
            CategoryTreeNode parent = nodeMap.get(pid);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    private CategoryTreeNode toNode(Category category) {
        CategoryTreeNode node = new CategoryTreeNode();
        node.setId(category.getId());
        node.setName(category.getName());
        node.setCode(category.getCode());
        node.setParentId(category.getParentId());
        node.setLevel(category.getLevel());
        node.setImageUrl(category.getImageUrl());
        node.setSort(category.getSort());
        node.setStatus(category.getStatus());
        return node;
    }

    private int calcLevel(Long parentId, Long selfId) {
        if (parentId == null || parentId == 0L) {
            return 1;
        }
        if (selfId != null && selfId.equals(parentId)) {
            throw new BizException("Parent category cannot be self");
        }
        Category parent = requireCategory(parentId);
        if (parent.getLevel() == null) {
            throw new BizException("Parent category level is invalid");
        }
        int level = parent.getLevel() + 1;
        if (level > 3) {
            throw new BizException("Category level cannot exceed 3");
        }
        return level;
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
