package com.kairos.application.payment;

import com.kairos.domain.order.Order;
import com.kairos.domain.order.OrderRepository;
import com.kairos.domain.payment.Payment;
import com.kairos.domain.payment.PaymentRepository;
import com.kairos.infrastructure.external.TossPaymentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentCommandService implements PaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TossPaymentClient tossPaymentClient;

    @Override
    public PaymentResponse confirmPayment(ConfirmPaymentCommand command) {
        // 1. 토스 결제 승인 API 호출
        tossPaymentClient.confirm(
                command.paymentKey(),
                command.orderId(),
                command.amount()
        );

        // 2. 주문 찾기
        Order order = orderRepository.findById(Long.valueOf(command.orderId().split("_")[1]))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // 3. 결제 정보 저장
        Payment payment = Payment.builder()
                .order(order)
                .paymentKey(command.paymentKey())
                .tossOrderId(command.orderId())
                .amount(command.amount())
                .build();

        // 4. 주문 상태 변경
        order.changeStatus(Order.OrderStatus.PAID);
        orderRepository.save(order);

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse cancelPayment(String paymentKey, String cancelReason) {
        // 1. 토스 결제 취소 API 호출
        tossPaymentClient.cancel(paymentKey, cancelReason);

        // 2. 결제 정보 업데이트
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        payment.cancel();

        // 3. 주문 상태 변경
        payment.getOrder().changeStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(payment.getOrder());

        return PaymentResponse.from(paymentRepository.save(payment));
    }
}