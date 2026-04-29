package com.kairos.domain.payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByTossOrderId(String tossOrderId);
    Optional<Payment> findByPaymentKey(String paymentKey);
}