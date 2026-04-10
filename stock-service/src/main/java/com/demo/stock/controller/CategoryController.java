package com.demo.stock.controller;

import com.demo.common.api.ApiResponse;
import com.demo.stock.dto.CategoryCreateRequest;
import com.demo.stock.dto.CategoryTreeNode;
import com.demo.stock.dto.CategoryUpdateRequest;
import com.demo.common.entity.Category;
import com.demo.stock.service.CategoryService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ApiResponse<Category> create(@Valid @RequestBody CategoryCreateRequest request) {
        return ApiResponse.ok("Category created", categoryService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> update(@PathVariable Long id,
                                        @Valid @RequestBody CategoryUpdateRequest request) {
        return ApiResponse.ok("Category updated", categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.ok("Category deleted", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<Category> detail(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.detail(id));
    }

    @GetMapping
    public ApiResponse<List<Category>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(categoryService.list(keyword));
    }

    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeNode>> tree() {
        return ApiResponse.ok(categoryService.tree());
    }
}
