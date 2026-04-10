package com.demo.stock.service;

import com.demo.stock.dto.CategoryCreateRequest;
import com.demo.stock.dto.CategoryTreeNode;
import com.demo.stock.dto.CategoryUpdateRequest;
import com.demo.common.entity.Category;
import java.util.List;

public interface CategoryService {

    Category create(CategoryCreateRequest request);

    Category update(Long id, CategoryUpdateRequest request);

    void delete(Long id);

    Category detail(Long id);

    List<Category> list(String keyword);

    List<CategoryTreeNode> tree();
}
