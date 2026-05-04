package com.kairos.domain.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String token;
    private boolean verified;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    @Builder
    public EmailVerification(String email, String token) {
        this.email = email;
        this.token = token;
        this.verified = false;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = LocalDateTime.now().plusHours(24); // 24시간 유효
    }

    // 도메인 로직 - 인증 완료
    public void verify() {
        this.verified = true;
    }

    // 도메인 로직 - 만료 여부
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }
}