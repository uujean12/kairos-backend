package com.kairos.application.order;

import com.kairos.domain.order.Order;
import com.kairos.domain.order.OrderItem;
import com.kairos.domain.order.OrderRepository;
import com.kairos.domain.product.Product;
import com.kairos.domain.product.ProductRepository;
import com.kairos.domain.user.User;
import com.kairos.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderCommandService implements OrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders(Long userId) {
        User user = getUser(userId);
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId, Long userId) {
        User user = getUser(userId);
        return orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }

    @Override
    public Order createOrder(CreateOrderCommand command) {
        User user = getUser(command.userId());

        Order order = Order.builder()
                .user(user)
                .recipient(command.recipient())
                .phone(command.phone())
                .address(command.address())
                .addressDetail(command.addressDetail())
                .memo(command.memo())
                .build();

        int total = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemCommand itemCommand : command.items()) {
            Product product = productRepository.findById(itemCommand.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

            // 도메인 로직 - 재고 차감
            product.decreaseStock(itemCommand.quantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(itemCommand.quantity())
                    .build();

            orderItems.add(orderItem);
            total += product.getPrice() * itemCommand.quantity();
        }

        // 배송비 적용
        if (total < 50000) total += 3000;

        order.setTotalPrice(total);
        order.getItems().addAll(orderItems);

        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long orderId, Long userId) {
        Order order = getOrder(orderId, userId);

        if (order.getStatus() == Order.OrderStatus.SHIPPED ||
                order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송 중이거나 완료된 주문은 취소할 수 없습니다.");
        }

        // 도메인 로직 - 재고 복원
        order.getItems().forEach(item -> {
            item.getProduct().increaseStock(item.getQuantity());
            productRepository.save(item.getProduct());
        });

        order.cancel();
        return orderRepository.save(order);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}