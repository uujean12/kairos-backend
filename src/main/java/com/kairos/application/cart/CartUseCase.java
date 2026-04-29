package com.kairos.application.cart;

import com.kairos.domain.cart.CartItem;
import java.util.List;

public interface CartUseCase {
    List<CartItem> getCart(Long userId);
    CartItem addToCart(AddToCartCommand command);
    CartItem updateQuantity(Long cartItemId, Long userId, int quantity);
    void removeItem(Long cartItemId, Long userId);
    void clearCart(Long userId);
}