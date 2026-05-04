package com.kairos.application.email;

import com.kairos.domain.user.EmailVerification;
import com.kairos.domain.user.EmailVerificationRepository;
import com.kairos.domain.user.UserRepository;
import com.kairos.infrastructure.external.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;

    // 인증 메일 발송
    public void sendVerificationEmail(String email) {
        // 기존 인증 정보 삭제
        emailVerificationRepository.deleteByEmail(email);

        // 토큰 생성
        String token = UUID.randomUUID().toString();

        // 인증 정보 저장
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .token(token)
                .build();
        emailVerificationRepository.save(verification);

        // 이메일 발송
        emailSender.sendVerificationEmail(email, token);
    }

    // 이메일 인증 확인
    public boolean verifyEmail(String token) {
        EmailVerification verification = emailVerificationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

        if (verification.isExpired()) {
            throw new IllegalStateException("만료된 인증 링크입니다. 다시 인증 메일을 요청해주세요.");
        }

        // 인증 완료 처리
        verification.verify();
        emailVerificationRepository.save(verification);

        // 유저 이메일 인증 상태 업데이트
        userRepository.findByEmail(verification.getEmail())
                .ifPresent(user -> {
                    user.verifyEmail();
                    userRepository.save(user);
                });

        return true;
    }

    // 인증 여부 확인
    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        return emailVerificationRepository.findByEmail(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }
}