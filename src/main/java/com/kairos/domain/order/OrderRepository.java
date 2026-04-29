package com.kairos.domain.order;

import com.kairos.domain.user.User;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    Optional<Order> findByIdAndUser(Long id, User user);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    long count();
    Order save(Order order);
}