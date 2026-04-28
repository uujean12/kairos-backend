package com.kairos.controller;

import com.kairos.entity.User;
import com.kairos.repository.UserRepository;
import com.kairos.security.JwtTokenProvider;
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
            return ResponseEntity.badRequest().body(Map.of("message", "이미 사용 중인 이메일입니다."));
        }

        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .provider(User.AuthProvider.LOCAL)
                .build());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        return ResponseEntity.ok(Map.of("accessToken", token, "user", toUserMap(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        return userRepository.findByEmail(email)
                .filter(u -> u.getPassword() != null && passwordEncoder.matches(password, u.getPassword()))
                .map(u -> {
                    String token = jwtTokenProvider.generateToken(u.getId(), u.getEmail());
                    return ResponseEntity.ok(Map.of("accessToken", token, "user", toUserMap(u)));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("message", "이메일 또는 비밀번호가 올바르지 않습니다.")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Stateless - client deletes token
        return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(toUserMap(user));
    }

    private Map<String, Object> toUserMap(User u) {
        return Map.of("id", u.getId(), "email", u.getEmail(), "name", u.getName(),
                "role", u.getRole().name());
    }
}
