package com.kairos.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Page<Product> findByActiveTrue(Pageable pageable);
    Optional<Product> findById(Long id);
    List<Product> findByCategoryIgnoreCaseAndActiveTrue(String category);
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String keyword);
    Product save(Product product);
    long countActive();
}