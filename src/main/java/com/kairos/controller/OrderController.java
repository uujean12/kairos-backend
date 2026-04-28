package com.kairos.controller;

import com.kairos.entity.*;
import com.kairos.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    @GetMapping
    public ResponseEntity<?> getOrders(@AuthenticationPrincipal User user) {
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(orders.stream().map(this::toOrderDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return orderRepository.findByIdAndUser(id, user)
                .map(order -> ResponseEntity.ok(toOrderDto(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal User user,
                                         @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) body.get("items");
        @SuppressWarnings("unchecked")
        Map<String, String> shippingInfo = (Map<String, String>) body.get("shippingInfo");

        if (itemsData == null || itemsData.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "주문 항목이 없습니다."));
        }

        // Build order
        Order order = Order.builder()
                .user(user)
                .recipient(shippingInfo.get("recipient"))
                .phone(shippingInfo.get("phone"))
                .address(shippingInfo.get("address"))
                .addressDetail(shippingInfo.get("addressDetail"))
                .memo(shippingInfo.get("memo"))
                .build();

        int total = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map<String, Object> itemData : itemsData) {
            Long productId = Long.valueOf(itemData.get("productId").toString());
            int quantity = Integer.parseInt(itemData.get("quantity").toString());

            // 재고 확인 및 차감 (트랜잭션)
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productId));

            if (product.getStock() < quantity) {
                throw new RuntimeException("재고 부족: " + product.getName());
            }

            product.setStock(product.getStock() - quantity);
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(quantity)
                    .build();

            orderItems.add(orderItem);
            total += product.getPrice() * quantity;
        }

        // 배송비 적용
        if (total < 50000) total += 3000;

        order.setTotalPrice(total);
        order.getItems().addAll(orderItems);

        Order saved = orderRepository.save(order);

        // 장바구니 비우기
        cartItemRepository.deleteByUser(user);

        return ResponseEntity.ok(toOrderDto(saved));
    }

    @PutMapping("/{id}/cancel")
    @Transactional
    public ResponseEntity<?> cancelOrder(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return orderRepository.findByIdAndUser(id, user)
                .map(order -> {
                    if (order.getStatus() == Order.OrderStatus.SHIPPED ||
                        order.getStatus() == Order.OrderStatus.DELIVERED) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "배송 중이거나 완료된 주문은 취소할 수 없습니다."));
                    }
                    // 재고 복원
                    order.getItems().forEach(item -> {
                        item.getProduct().setStock(item.getProduct().getStock() + item.getQuantity());
                        productRepository.save(item.getProduct());
                    });
                    order.setStatus(Order.OrderStatus.CANCELLED);
                    return ResponseEntity.ok(toOrderDto(orderRepository.save(order)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toOrderDto(Order order) {
        List<Map<String, Object>> itemDtos = order.getItems().stream().map(item ->
                Map.<String, Object>of(
                        "productId", item.getProduct().getId(),
                        "productName", item.getProductName(),
                        "price", item.getPrice(),
                        "quantity", item.getQuantity()
                )).toList();

        Map<String, Object> shippingInfo = new HashMap<>();
        shippingInfo.put("recipient", order.getRecipient());
        shippingInfo.put("phone", order.getPhone());
        shippingInfo.put("address", order.getAddress());
        shippingInfo.put("addressDetail", order.getAddressDetail());

        return Map.of(
                "id", order.getId(),
                "orderNumber", order.getOrderNumber(),
                "items", itemDtos,
                "totalPrice", order.getTotalPrice(),
                "status", order.getStatus().name(),
                "shippingInfo", shippingInfo,
                "createdAt", order.getCreatedAt().toString()
        );
    }
}
