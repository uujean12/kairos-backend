package com.kairos.infrastructure.web;

import com.kairos.application.order.CreateOrderCommand;
import com.kairos.application.order.OrderItemCommand;
import com.kairos.application.order.OrderUseCase;
import com.kairos.domain.order.Order;
import com.kairos.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;

    @GetMapping
    public ResponseEntity<?> getOrders(@AuthenticationPrincipal User user) {
        List<Order> orders = orderUseCase.getOrders(user.getId());
        return ResponseEntity.ok(orders.stream().map(this::toOrderDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal User user,
                                      @PathVariable Long id) {
        Order order = orderUseCase.getOrder(id, user.getId());
        return ResponseEntity.ok(toOrderDto(order));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal User user,
                                         @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) body.get("items");
        @SuppressWarnings("unchecked")
        Map<String, String> shippingInfo = (Map<String, String>) body.get("shippingInfo");

        if (itemsData == null || itemsData.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "주문 항목이 없습니다."));
        }

        List<OrderItemCommand> items = itemsData.stream()
                .map(item -> new OrderItemCommand(
                        Long.valueOf(item.get("productId").toString()),
                        Integer.parseInt(item.get("quantity").toString())
                )).collect(Collectors.toList());

        CreateOrderCommand command = new CreateOrderCommand(
                user.getId(),
                items,
                shippingInfo.get("recipient"),
                shippingInfo.get("phone"),
                shippingInfo.get("address"),
                shippingInfo.get("addressDetail"),
                shippingInfo.get("memo")
        );

        Order order = orderUseCase.createOrder(command);
        return ResponseEntity.ok(toOrderDto(order));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@AuthenticationPrincipal User user,
                                         @PathVariable Long id) {
        Order order = orderUseCase.cancelOrder(id, user.getId());
        return ResponseEntity.ok(toOrderDto(order));
    }

    private Map<String, Object> toOrderDto(Order order) {
        List<Map<String, Object>> items = order.getItems().stream().map(item ->
                Map.<String, Object>of(
                        "productId", item.getProduct().getId(),
                        "productName", item.getProductName(),
                        "price", item.getPrice(),
                        "quantity", item.getQuantity()
                )).collect(Collectors.toList());

        Map<String, Object> shippingInfo = new HashMap<>();
        shippingInfo.put("recipient", order.getRecipient());
        shippingInfo.put("phone", order.getPhone());
        shippingInfo.put("address", order.getAddress());
        shippingInfo.put("addressDetail", order.getAddressDetail());

        return Map.of(
                "id", order.getId(),
                "orderNumber", order.getOrderNumber(),
                "items", items,
                "totalPrice", order.getTotalPrice(),
                "status", order.getStatus().name(),
                "shippingInfo", shippingInfo,
                "createdAt", order.getCreatedAt().toString()
        );
    }
}