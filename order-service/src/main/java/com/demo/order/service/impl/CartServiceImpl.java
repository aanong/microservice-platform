package com.demo.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.common.entity.Product;
import com.demo.order.cache.OrderCacheKeys;
import com.demo.common.cache.RedisJsonCacheHelper;
import com.demo.order.dto.AddCartItemRequest;
import com.demo.order.entity.CartItem;
import com.demo.order.exception.BizException;
import com.demo.order.mapper.CartItemMapper;
import com.demo.order.mapper.ProductStockMapper;
import com.demo.order.service.CartService;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductStockMapper productStockMapper;
    private final RedisJsonCacheHelper cacheHelper;

    public CartServiceImpl(CartItemMapper cartItemMapper,
                           ProductStockMapper productStockMapper,
                           RedisJsonCacheHelper cacheHelper) {
        this.cartItemMapper = cartItemMapper;
        this.productStockMapper = productStockMapper;
        this.cacheHelper = cacheHelper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItem add(AddCartItemRequest request) {
        Product product = productStockMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BizException("Product not found: " + request.getProductId());
        }
        if (product.getStock() == null || product.getStock() < request.getQuantity()) {
            throw new BizException("Insufficient stock for product: " + request.getProductId());
        }

        CartItem exists = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, request.getUserId())
            .eq(CartItem::getProductId, request.getProductId()));

        if (exists != null) {
            exists.setQuantity(exists.getQuantity() + request.getQuantity());
            exists.setPrice(product.getPrice());
            exists.setUpdateTime(LocalDateTime.now());
            cartItemMapper.updateById(exists);
            return exists;
        }

        CartItem cartItem = new CartItem();
        cartItem.setUserId(request.getUserId());
        cartItem.setProductId(product.getId());
        cartItem.setProductName(product.getName());
        cartItem.setSkuCode(product.getSkuCode());
        cartItem.setPrice(product.getPrice());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setChecked(1);
        cartItem.setCreateTime(LocalDateTime.now());
        cartItem.setUpdateTime(LocalDateTime.now());
        cartItemMapper.insert(cartItem);
        return cartItem;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new BizException("Cart item not found");
        }
        cartItemMapper.deleteById(cartItemId);
    }

    @Override
    public List<CartItem> list(Long userId) {
        String key = OrderCacheKeys.cartList(userId);
        List<CartItem> cached = cacheHelper.getList(key, new TypeReference<List<CartItem>>() {
        });
        if (cached != null) {
            return cached;
        }

        List<CartItem> cartItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, userId)
            .orderByDesc(CartItem::getId));
        cacheHelper.setList(key, cartItems);
        return cartItems;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear(Long userId) {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
    }
}
