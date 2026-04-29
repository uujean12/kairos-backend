package com.kairos.domain.payment;

import com.kairos.domain.order.Order;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private String paymentKey;      // 토스 결제 키
    @Column(name = "toss_order_id")
    private String tossOrderId;         // 토스 주문 ID
    private Integer amount;         // 결제 금액

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentStatus {
        PENDING,    // 결제 대기
        DONE,       // 결제 완료
        CANCELLED,  // 결제 취소
        FAILED      // 결제 실패
    }

    @Builder
    public Payment(Order order, String paymentKey, String tossOrderId, Integer amount) {
        this.order = order;
        this.paymentKey = paymentKey;
        this.tossOrderId = tossOrderId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // 도메인 로직 - 결제 완료
    public void complete() {
        this.status = PaymentStatus.DONE;
        this.paidAt = LocalDateTime.now();
    }

    // 도메인 로직 - 결제 취소
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }

    // 도메인 로직 - 결제 실패
    public void fail() {
        this.status = PaymentStatus.FAILED;
    }
}