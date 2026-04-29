package com.kairos.infrastructure.web;

import com.kairos.application.cart.AddToCartCommand;
import com.kairos.application.cart.CartUseCase;
import com.kairos.domain.cart.CartItem;
import com.kairos.domain.user.User;
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

    private final CartUseCase cartUseCase;

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal User user) {
        List<CartItem> items = cartUseCase.getCart(user.getId());
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

        CartItem cartItem = cartUseCase.addToCart(
                new AddToCartCommand(user.getId(), productId, quantity));
        return ResponseEntity.ok(cartItem);
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<?> updateQuantity(@AuthenticationPrincipal User user,
                                            @PathVariable Long cartItemId,
                                            @RequestBody Map<String, Object> body) {
        int quantity = Integer.parseInt(body.get("quantity").toString());
        CartItem cartItem = cartUseCase.updateQuantity(cartItemId, user.getId(), quantity);
        return cartItem != null ? ResponseEntity.ok(cartItem) : ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeItem(@AuthenticationPrincipal User user,
                                        @PathVariable Long cartItemId) {
        cartUseCase.removeItem(cartItemId, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal User user) {
        cartUseCase.clearCart(user.getId());
        return ResponseEntity.ok().build();
    }
}