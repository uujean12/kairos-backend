package com.kairos.security;

import com.kairos.entity.User;
import com.kairos.repository.UserRepository;
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

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .provider(registrationId.equals("google")
                                        ? User.AuthProvider.GOOGLE : User.AuthProvider.KAKAO)
                                .build()
                ));

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        // Redirect to frontend with token
        String frontendUrl = allowedOrigins.split(",")[0].trim();
        getRedirectStrategy().sendRedirect(request, response,
                frontendUrl + "/oauth2/callback?token=" + token);
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
