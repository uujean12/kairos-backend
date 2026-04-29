package com.kairos.infrastructure.persistence;

import com.kairos.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    Page<Product> findByActiveTrue(Pageable pageable);
    List<Product> findByCategoryIgnoreCaseAndActiveTrue(String category);
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String keyword);
    long countByActiveTrue();
}