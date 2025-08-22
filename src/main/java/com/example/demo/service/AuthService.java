
package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.domain.utils.AuthProvider;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository; // [추가]
    private final PasswordEncoder passwordEncoder; // [추가]

    // [추가] 회원가입: 이메일 중복 검사 → 암호화 저장 → 세션 로그인(선택)
    public User signup(String name, String email, String rawPassword, HttpSession session) {
        Optional<User> exists = userRepository.findByEmail(email);
        if (exists.isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword)); // [추가] BCrypt 저장
        u.setProvider(AuthProvider.LOCAL);
        u.setProviderId(null);

        // 선택 필드(널 허용이면 무시 가능)
        if (u.getBirthDate() == null) u.setBirthDate(LocalDate.now());

        User saved = userRepository.save(u);

        // [추가] 가입 직후 자동 로그인 (원치 않으면 제거)
        session.setAttribute("USER_ID", saved.getId());

        return saved;
    }

    // [추가] 로그인: 이메일 조회 → 비번 매칭 → 세션 저장
    public User login(String email, String rawPassword, HttpSession session) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (u.getPassword() == null || !passwordEncoder.matches(rawPassword, u.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }
        session.setAttribute("USER_ID", u.getId());
        return u;
    }

    // [추가] 세션 기반 현재 유저 조회
    public User me(HttpSession session) {
        Object id = session.getAttribute("USER_ID");
        if (id == null) return null;
        return userRepository.findById((Long) id).orElse(null);
    }

    // [추가] 로그아웃
    public void logout(HttpSession session) {
        session.invalidate();
    }
}