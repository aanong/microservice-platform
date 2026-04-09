package com.demo.order.service;

import com.demo.order.dto.AddCartItemRequest;
import com.demo.order.entity.CartItem;
import java.util.List;

public interface CartService {

    CartItem add(AddCartItemRequest request);

    void remove(Long userId, Long cartItemId);

    List<CartItem> list(Long userId);

    void clear(Long userId);
}
