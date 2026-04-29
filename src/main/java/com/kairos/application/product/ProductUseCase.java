package com.kairos.application.product;

import com.kairos.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductUseCase {
    Page<Product> getProducts(Pageable pageable);
    Product getProduct(Long id);
    List<Product> getProductsByCategory(String category);
    List<Product> searchProducts(String keyword);
    Product createProduct(ProductCommand command);
    Product updateProduct(Long id, ProductCommand command);
    void deleteProduct(Long id);
}