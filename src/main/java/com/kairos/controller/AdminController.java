package com.kairos.controller;

import com.kairos.entity.Order;
import com.kairos.entity.User;
import com.kairos.repository.OrderRepository;
import com.kairos.repository.ProductRepository;
import com.kairos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // ─── 대시보드 통계 ────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long products = productRepository.count();
        long orders   = orderRepository.count();
        long users    = userRepository.count();
        long revenue  = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != Order.OrderStatus.CANCELLED)
                .mapToLong(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0)
                .sum();

        return ResponseEntity.ok(Map.of(
                "products", products,
                "orders",   orders,
                "users",    users,
                "revenue",  revenue
        ));
    }

    // ─── 주문 전체 조회 ────────────────────────────────
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        List<Map<String, Object>> result = orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .map(order -> {
                    List<Map<String, Object>> items = order.getItems().stream().map(item ->
                            Map.<String, Object>of(
                                    "productId",   item.getProduct().getId(),
                                    "productName", item.getProductName(),
                                    "price",       item.getPrice(),
                                    "quantity",    item.getQuantity()
                            )).collect(Collectors.toList());

                    Map<String, Object> dto = new LinkedHashMap<>();
                    dto.put("id",          order.getId());
                    dto.put("orderNumber", order.getOrderNumber());
                    dto.put("userEmail",   order.getUser().getEmail());
                    dto.put("recipient",   order.getRecipient());
                    dto.put("phone",       order.getPhone());
                    dto.put("address",     order.getAddress());
                    dto.put("addressDetail", order.getAddressDetail());
                    dto.put("memo",        order.getMemo());
                    dto.put("totalPrice",  order.getTotalPrice());
                    dto.put("status",      order.getStatus().name());
                    dto.put("items",       items);
                    dto.put("createdAt",   order.getCreatedAt().toString());
                    return dto;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ─── 주문 상태 변경 ────────────────────────────────
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id,
                                               @RequestBody Map<String, String> body) {
        return orderRepository.findById(id).map(order -> {
            try {
                order.setStatus(Order.OrderStatus.valueOf(body.get("status")));
                return ResponseEntity.ok(Map.of("message", "상태가 변경되었습니다."));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.<Object>badRequest().body(Map.of("message", "유효하지 않은 상태입니다."));
            } finally {
                orderRepository.save(order);
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── 회원 전체 조회 ────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<Map<String, Object>> result = userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .map(u -> {
                    Map<String, Object> dto = new LinkedHashMap<>();
                    dto.put("id",        u.getId());
                    dto.put("name",      u.getName());
                    dto.put("email",     u.getEmail());
                    dto.put("role",      u.getRole().name());
                    dto.put("provider",  u.getProvider() != null ? u.getProvider().name() : "LOCAL");
                    dto.put("createdAt", u.getCreatedAt().toString());
                    return dto;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ─── 회원 권한 변경 ────────────────────────────────
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(u -> {
            try {
                u.setRole(User.Role.valueOf(body.get("role")));
                userRepository.save(u);
                return ResponseEntity.ok(Map.of("message", "권한이 변경되었습니다."));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.<Object>badRequest().body(Map.of("message", "유효하지 않은 권한입니다."));
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}
