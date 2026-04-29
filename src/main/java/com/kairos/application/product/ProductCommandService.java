package com.kairos.application.product;

import com.kairos.domain.product.Product;
import com.kairos.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCommandService implements ProductUseCase {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCaseAndActiveTrue(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword);
    }

    @Override
    public Product createProduct(ProductCommand command) {
        Product product = Product.builder()
                .name(command.name())
                .description(command.description())
                .price(command.price())
                .stock(command.stock())
                .category(command.category())
                .imageUrl(command.imageUrl())
                .build();
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, ProductCommand command) {
        Product product = getProduct(id);
        product.update(
                command.name(),
                command.description(),
                command.price(),
                command.stock(),
                command.category(),
                command.imageUrl()
        );
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = getProduct(id);
        product.deactivate();
        productRepository.save(product);
    }
}