package com.kairos.infrastructure.security;

import com.kairos.domain.user.User;
import com.kairos.domain.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = extractEmail(oAuth2User);
        String name = extractName(oAuth2User);
        String registrationId = extractRegistrationId(request);
        String frontendUrl = allowedOrigins.split(",")[0].trim();

        if (registrationId.equals("kakao")) {
            // 카카오는 추가 정보 입력 페이지로
            String kakaoId = String.valueOf(oAuth2User.getAttributes().get("id"));
            getRedirectStrategy().sendRedirect(request, response,
                    frontendUrl + "/additional-info?provider=kakao&kakaoId=" + kakaoId);
            return;
        }

        // Google은 기존 회원이면 바로 로그인
        if (userRepository.findByEmail(email).isPresent()) {
            User user = userRepository.findByEmail(email).get();
            String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
            getRedirectStrategy().sendRedirect(request, response,
                    frontendUrl + "/oauth2/callback?token=" + token);
            return;
        }

        // Google 신규 회원은 개인정보 동의 페이지로
        String tempEmail = java.net.URLEncoder.encode(email, "UTF-8");
        String tempName = java.net.URLEncoder.encode(name, "UTF-8");
        getRedirectStrategy().sendRedirect(request, response,
                frontendUrl + "/additional-info?provider=google&email=" + tempEmail + "&name=" + tempName);
    }

    private String extractEmail(OAuth2User user) {
        Map<String, Object> attrs = user.getAttributes();
        if (attrs.containsKey("email")) return (String) attrs.get("email");
        // Kakao
        Map<String, Object> kakaoAccount = (Map<String, Object>) attrs.get("kakao_account");
        if (kakaoAccount != null) return (String) kakaoAccount.get("email");
        return null;
    }

    private String extractName(OAuth2User user) {
        Map<String, Object> attrs = user.getAttributes();
        if (attrs.containsKey("name")) return (String) attrs.get("name");
        // Kakao
        Map<String, Object> properties = (Map<String, Object>) attrs.get("properties");
        if (properties != null) return (String) properties.get("nickname");
        return "사용자";
    }

    private String extractRegistrationId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("google")) return "google";
        if (uri.contains("kakao")) return "kakao";
        return "unknown";
    }
}
