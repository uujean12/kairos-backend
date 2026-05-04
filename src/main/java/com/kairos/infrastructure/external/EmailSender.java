package com.kairos.infrastructure.external;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[kairos] 이메일 인증을 완료해주세요");
            helper.setText(buildEmailContent(token), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송 실패: " + e.getMessage());
        }
    }

    private String buildEmailContent(String token) {
        String verifyUrl = "http://localhost:8080/api/auth/verify?token=" + token;
        return """
                <div style="font-family: 'Arial', sans-serif; max-width: 600px; margin: 0 auto; padding: 40px 20px;">
                    <h1 style="font-size: 24px; color: #111; text-align: center; letter-spacing: 4px;">KAIROS</h1>
                    <hr style="border: 1px solid #f0f0f0; margin: 24px 0;">
                    <p style="font-size: 15px; color: #444; line-height: 1.8;">안녕하세요.<br>아래 버튼을 클릭하여 이메일 인증을 완료해주세요.</p>
                    <div style="text-align: center; margin: 32px 0;">
                        <a href="%s"
                           style="background: #1a1a1a; color: #fff; padding: 14px 40px;
                                  text-decoration: none; font-size: 14px; letter-spacing: 1px;">
                            이메일 인증하기
                        </a>
                    </div>
                    <p style="font-size: 12px; color: #999; text-align: center;">
                        링크는 24시간 동안 유효합니다.<br>
                        본인이 요청하지 않은 경우 이 메일을 무시해주세요.
                    </p>
                </div>
                """.formatted(verifyUrl);
    }
}