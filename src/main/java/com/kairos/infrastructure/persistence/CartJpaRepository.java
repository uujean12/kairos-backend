package com.kairos.infrastructure.persistence;

import com.kairos.domain.cart.CartItem;
import com.kairos.domain.product.Product;
import com.kairos.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    void deleteByUser(User user);
}