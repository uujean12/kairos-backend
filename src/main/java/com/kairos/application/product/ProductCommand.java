package com.kairos.application.product;

public record ProductCommand(
        String name,
        String description,
        Integer price,
        Integer stock,
        String category,
        String imageUrl
) {}