package com.kairos.infrastructure.persistence;

import com.kairos.domain.order.Order;
import com.kairos.domain.order.OrderRepository;
import com.kairos.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public List<Order> findByUserOrderByCreatedAtDesc(User user) {
        return jpaRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public Optional<Order> findByIdAndUser(Long id, User user) {
        return jpaRepository.findByIdAndUser(id, user);
    }

    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id);
    }
}