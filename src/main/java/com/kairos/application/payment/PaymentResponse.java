package com.kairos.application.payment;

import com.kairos.domain.payment.Payment;

public record PaymentResponse(
        Long id,
        String paymentKey,
        String orderId,
        Integer amount,
        String status,
        String paidAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentKey(),
                payment.getTossOrderId(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getPaidAt() != null ? payment.getPaidAt().toString() : null
        );
    }
}