package com.kairos.infrastructure.web;

import com.kairos.application.payment.ConfirmPaymentCommand;
import com.kairos.application.payment.PaymentResponse;
import com.kairos.application.payment.PaymentUseCase;
import com.kairos.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    // 결제 승인
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {

        ConfirmPaymentCommand command = new ConfirmPaymentCommand(
                body.get("paymentKey").toString(),
                body.get("orderId").toString(),
                Integer.parseInt(body.get("amount").toString()),
                user.getId()
        );

        return ResponseEntity.ok(paymentUseCase.confirmPayment(command));
    }

    // 결제 취소
    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody Map<String, String> body) {

        return ResponseEntity.ok(paymentUseCase.cancelPayment(
                paymentKey,
                body.getOrDefault("cancelReason", "고객 요청 취소")
        ));
    }
}