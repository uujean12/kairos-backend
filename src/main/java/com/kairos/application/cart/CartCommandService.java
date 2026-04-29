package com.kairos.application.cart;

import com.kairos.domain.cart.CartItem;
import com.kairos.domain.cart.CartRepository;
import com.kairos.domain.product.Product;
import com.kairos.domain.product.ProductRepository;
import com.kairos.domain.user.User;
import com.kairos.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartCommandService implements CartUseCase {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCart(Long userId) {
        User user = getUser(userId);
        return cartRepository.findByUser(user);
    }

    @Override
    public CartItem addToCart(AddToCartCommand command) {
        User user = getUser(command.userId());
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (product.getStock() < command.quantity()) {
            throw new IllegalStateException("재고가 부족합니다.");
        }

        CartItem cartItem = cartRepository.findByUserAndProduct(user, product)
                .map(existing -> {
                    existing.addQuantity(command.quantity());
                    return existing;
                })
                .orElse(CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(command.quantity())
                        .build());

        return cartRepository.save(cartItem);
    }

    @Override
    public CartItem updateQuantity(Long cartItemId, Long userId, int quantity) {
        CartItem cartItem = getCartItem(cartItemId, userId);
        if (quantity <= 0) {
            cartRepository.delete(cartItem);
            return null;
        }
        cartItem.updateQuantity(quantity);
        return cartRepository.save(cartItem);
    }

    @Override
    public void removeItem(Long cartItemId, Long userId) {
        CartItem cartItem = getCartItem(cartItemId, userId);
        cartRepository.delete(cartItem);
    }

    @Override
    public void clearCart(Long userId) {
        User user = getUser(userId);
        cartRepository.deleteByUser(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private CartItem getCartItem(Long cartItemId, Long userId) {
        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new IllegalStateException("접근 권한이 없습니다.");
        }
        return cartItem;
    }
}