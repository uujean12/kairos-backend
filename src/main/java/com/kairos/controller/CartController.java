package com.kairos.controller;

import com.kairos.entity.CartItem;
import com.kairos.entity.Product;
import com.kairos.entity.User;
import com.kairos.repository.CartItemRepository;
import com.kairos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);
        int totalCount = items.stream().mapToInt(CartItem::getQuantity).sum();

        List<Map<String, Object>> itemDtos = items.stream().map(item -> Map.<String, Object>of(
                "id", item.getId(),
                "productId", item.getProduct().getId(),
                "name", item.getProduct().getName(),
                "price", item.getProduct().getPrice(),
                "imageUrl", item.getProduct().getImageUrl() != null ? item.getProduct().getImageUrl() : "",
                "quantity", item.getQuantity(),
                "stock", item.getProduct().getStock()
        )).toList();

        return ResponseEntity.ok(Map.of("items", itemDtos, "totalCount", totalCount));
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@AuthenticationPrincipal User user,
                                       @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        if (product.getStock() < quantity) {
            return ResponseEntity.badRequest().body(Map.of("message", "재고가 부족합니다."));
        }

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + quantity);
                    return existing;
                })
                .orElse(CartItem.builder().user(user).product(product).quantity(quantity).build());

        return ResponseEntity.ok(cartItemRepository.save(cartItem));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<?> updateQuantity(@AuthenticationPrincipal User user,
                                            @PathVariable Long cartItemId,
                                            @RequestBody Map<String, Object> body) {
        int quantity = Integer.parseInt(body.get("quantity").toString());

        return cartItemRepository.findById(cartItemId)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .map(item -> {
                    if (quantity <= 0) {
                        cartItemRepository.delete(item);
                        return ResponseEntity.ok().build();
                    }
                    item.setQuantity(quantity);
                    return ResponseEntity.ok(cartItemRepository.save(item));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeItem(@AuthenticationPrincipal User user,
                                        @PathVariable Long cartItemId) {
        cartItemRepository.findById(cartItemId)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .ifPresent(cartItemRepository::delete);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal User user) {
        cartItemRepository.deleteByUser(user);
        return ResponseEntity.ok().build();
    }
}
