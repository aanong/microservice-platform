package com.demo.stock.service;

import com.demo.stock.dto.ProductCreateRequest;
import com.demo.stock.dto.ProductUpdateRequest;
import com.demo.common.entity.Product;
import java.util.List;

public interface ProductService {

    Product create(ProductCreateRequest request);

    Product update(Long id, ProductUpdateRequest request);

    void delete(Long id);

    Product detail(Long id);

    List<Product> list(String keyword, Long categoryId);
}
