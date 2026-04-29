package com.kairos.application.order;

public record OrderItemCommand(
        Long productId,
        Integer quantity
) {}