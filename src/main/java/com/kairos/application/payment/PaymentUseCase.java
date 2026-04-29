package com.kairos.application.payment;

public interface PaymentUseCase {
    PaymentResponse confirmPayment(ConfirmPaymentCommand command);
    PaymentResponse cancelPayment(String paymentKey, String cancelReason);
}