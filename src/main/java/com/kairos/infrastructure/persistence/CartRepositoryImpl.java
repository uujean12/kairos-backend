package com.kairos.infrastructure.persistence;

import com.kairos.domain.cart.CartItem;
import com.kairos.domain.cart.CartRepository;
import com.kairos.domain.product.Product;
import com.kairos.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {

    private final CartJpaRepository jpaRepository;

    @Override
    public List<CartItem> findByUser(User user) {
        return jpaRepository.findByUser(user);
    }

    @Override
    public Optional<CartItem> findByUserAndProduct(User user, Product product) {
        return jpaRepository.findByUserAndProduct(user, product);
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return jpaRepository.save(cartItem);
    }

    @Override
    public void delete(CartItem cartItem) {
        jpaRepository.delete(cartItem);
    }

    @Override
    public void deleteByUser(User user) {
        jpaRepository.deleteByUser(user);
    }
}