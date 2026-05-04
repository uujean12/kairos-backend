package com.kairos.infrastructure.persistence;

import com.kairos.domain.user.EmailVerification;
import com.kairos.domain.user.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {

    private final EmailVerificationJpaRepository jpaRepository;

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        return jpaRepository.save(emailVerification);
    }

    @Override
    public Optional<EmailVerification> findByToken(String token) {
        return jpaRepository.findByToken(token);
    }

    @Override
    public Optional<EmailVerification> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public void deleteByEmail(String email) {
        jpaRepository.deleteByEmail(email);
    }
}