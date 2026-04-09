package com.demo.order.controller;

import com.demo.common.api.ApiResponse;
import com.demo.order.dto.AddCartItemRequest;
import com.demo.order.entity.CartItem;
import com.demo.order.security.UserContextHolder;
import com.demo.order.service.CartService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ApiResponse<CartItem> add(@Valid @RequestBody AddCartItemRequest request) {
        request.setUserId(UserContextHolder.requireUserId());
        return ApiResponse.ok("Added to cart", cartService.add(request));
    }

    @DeleteMapping("/{cartItemId}")
    public ApiResponse<Void> remove(@PathVariable Long cartItemId) {
        cartService.remove(UserContextHolder.requireUserId(), cartItemId);
        return ApiResponse.ok("Deleted", null);
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clear() {
        cartService.clear(UserContextHolder.requireUserId());
        return ApiResponse.ok("Cleared", null);
    }

    @GetMapping
    public ApiResponse<List<CartItem>> list() {
        return ApiResponse.ok(cartService.list(UserContextHolder.requireUserId()));
    }
}
