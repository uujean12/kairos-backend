package com.kairos.infrastructure.persistence;

import com.kairos.domain.product.Product;
import com.kairos.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    @Override
    public Page<Product> findByActiveTrue(Pageable pageable) {
        return jpaRepository.findByActiveTrue(pageable);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Product> findByCategoryIgnoreCaseAndActiveTrue(String category) {
        return jpaRepository.findByCategoryIgnoreCaseAndActiveTrue(category);
    }

    @Override
    public List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String keyword) {
        return jpaRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword);
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public long countActive() {
        return jpaRepository.countByActiveTrue();
    }
}