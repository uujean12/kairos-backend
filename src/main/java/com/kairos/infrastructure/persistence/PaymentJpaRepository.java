package com.kairos.infrastructure.persistence;

import com.kairos.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTossOrderId(String tossOrderId);
    Optional<Payment> findByPaymentKey(String paymentKey);
}