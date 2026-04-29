package com.kairos.domain.cart;

import com.kairos.domain.product.Product;
import com.kairos.domain.user.User;
import java.util.List;
import java.util.Optional;

public interface CartRepository {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    Optional<CartItem> findById(Long id);
    CartItem save(CartItem cartItem);
    void delete(CartItem cartItem);
    void deleteByUser(User user);
}