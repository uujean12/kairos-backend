package com.kairos.infrastructure.external;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Component
public class TossPaymentClient {

    @Value("${app.toss.secret-key}")
    private String secretKey;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "https://api.tosspayments.com/v1/payments";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private String getAuthHeader() {
        String credentials = secretKey + ":";
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    // 결제 승인
    public Map<String, Object> confirm(String paymentKey, String orderId, Integer amount) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "paymentKey", paymentKey,
                    "orderId", orderId,
                    "amount", amount
            ));

            Request request = new Request.Builder()
                    .url(BASE_URL + "/confirm")
                    .addHeader("Authorization", getAuthHeader())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body, JSON))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    throw new RuntimeException("결제 승인 실패: " + responseBody);
                }
                return objectMapper.readValue(responseBody, Map.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("토스 결제 API 오류: " + e.getMessage());
        }
    }

    // 결제 취소
    public Map<String, Object> cancel(String paymentKey, String cancelReason) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "cancelReason", cancelReason
            ));

            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + paymentKey + "/cancel")
                    .addHeader("Authorization", getAuthHeader())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body, JSON))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    throw new RuntimeException("결제 취소 실패: " + responseBody);
                }
                return objectMapper.readValue(responseBody, Map.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("토스 결제 API 오류: " + e.getMessage());
        }
    }
}