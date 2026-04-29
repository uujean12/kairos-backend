package com.kairos.application.order;

import com.kairos.domain.order.Order;
import java.util.List;

public interface OrderUseCase {
    List<Order> getOrders(Long userId);
    Order getOrder(Long orderId, Long userId);
    Order createOrder(CreateOrderCommand command);
    Order cancelOrder(Long orderId, Long userId);
}