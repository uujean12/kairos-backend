package com.kairos.domain.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Integer totalPrice;

    // Shipping info
    private String recipient;
    private String phone;
    private String address;
    private String addressDetail;
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum OrderStatus {
        PENDING, PAID, PREPARING, SHIPPED, DELIVERED, CANCELLED
    }

    @PrePersist
    public void generateOrderNumber() {
        if (this.orderNumber == null) {
            this.orderNumber = "KR" + System.currentTimeMillis();
        }
    }
    // 도메인 로직 - 주문 취소
    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    // 도메인 로직 - 상태 변경
    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

}
