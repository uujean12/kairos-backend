package com.kairos.application.cart;

public record AddToCartCommand(
        Long userId,
        Long productId,
        Integer quantity
) {}