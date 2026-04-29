package com.kairos.application.payment;

public record ConfirmPaymentCommand(
        String paymentKey,
        String orderId,
        Integer amount,
        Long userId
) {}