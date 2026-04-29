package com.kairos.infrastructure.web;

import com.kairos.domain.user.User;
import com.kairos.domain.user.UserRepository;
import com.kairos.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String name = body.get("name");

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "이미 사용 중인 이메일입니다."));
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .provider(User.AuthProvider.LOCAL)
                .build();

        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved.getId(), saved.getEmail());
        return ResponseEntity.ok(Map.of("accessToken", token, "user", toUserMap(saved)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        return userRepository.findByEmail(email)
                .filter(u -> u.getPassword() != null &&
                        passwordEncoder.matches(password, u.getPassword()))
                .map(u -> {
                    String token = jwtTokenProvider.generateToken(u.getId(), u.getEmail());
                    return ResponseEntity.ok(Map.of("accessToken", token, "user", toUserMap(u)));
                })
                .orElse(ResponseEntity.status(401)
                        .body(Map.of("message", "이메일 또는 비밀번호가 올바르지 않습니다.")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(toUserMap(user));
    }

    // 이름으로 이메일 찾기
    @PostMapping("/find-email")
    public ResponseEntity<?> findEmail(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        return userRepository.findAll().stream()
                .filter(u -> u.getName().equals(name))
                .findFirst()
                .map(u -> ResponseEntity.ok(Map.of("email", maskEmail(u.getEmail()))))
                .orElse(ResponseEntity.badRequest().body(Map.of("message", "일치하는 계정을 찾을 수 없습니다.")));
    }

    // 이메일 존재 확인
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.ok(Map.of("message", "이메일이 확인되었습니다."));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "등록된 이메일을 찾을 수 없습니다."));
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String newPassword = body.get("newPassword");

        return userRepository.findByEmail(email)
                .map(u -> {
                    u.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(u);
                    return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of("message", "이메일을 찾을 수 없습니다.")));
    }

    private Map<String, Object> toUserMap(User u) {
        return Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "name", u.getName(),
                "role", u.getRole().name()
        );
    }

    // 이메일 마스킹 (예: te***@gmail.com)
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}