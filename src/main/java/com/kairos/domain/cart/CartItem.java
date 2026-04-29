package com.kairos.domain.cart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.domain.product.Product;
import com.kairos.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CartItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    // 도메인 로직 - 수량 추가
    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    // 도메인 로직 - 수량 변경
    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

}
