package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.domain.utils.AuthProvider;
import com.example.demo.domain.utils.Gender;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public UserResponse signup(SignupRequest req) {
        String email = normalizeEmail(req.getEmail());
        String name = safeTrim(req.getName());
        String rawPw = req.getPassword();

        if (email == null) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (name == null) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        if (rawPw == null || rawPw.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        // 이메일 중복 체크
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPw))
                .provider(AuthProvider.LOCAL)
                .phone(emptyToNull(safeTrim(req.getPhone())))
                .birthDate(parseBirthDate(req.getBirthDate()))
                .gender(parseGender(req.getGender()))
                .build();

        User saved = userRepository.save(user);
        return toResponse(saved);
    }


    @Transactional(readOnly = true)
    public User authenticate(LoginRequest req) {
        String email = normalizeEmail(req.getEmail());
        if (email == null || req.getPassword() == null) {
            throw new IllegalArgumentException("이메일과 비밀번호를 확인하세요.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (user.getPassword() == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        return user;
    }


    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return toResponse(user);
    }


    // 안전하게 문자열 앞뒤 공백 제거
    private String safeTrim(String s) {
        return (s == null) ? null : s.trim();
    }

    // 문자열이 비어있으면 null 반환
    private String emptyToNull(String s) {
        String t = safeTrim(s);
        return t != null && t.isEmpty() ? null : t;
    }

    // 이메일 정규화(공백 제거 + 소문자)
    private String normalizeEmail(String raw) {
        String t = safeTrim(raw);
        return t == null ? null : t.toLowerCase();
    }

    // "yyyy-MM-dd" 형식의 문자열을 LocalDate로 파싱
    private LocalDate parseBirthDate(String raw) {
        raw = safeTrim(raw);
        if (raw == null) return null;
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            return null; // 형식 오류 시 null 반환
        }
    }

    // 문자열을 Gender enum으로 파싱(대소문자 무관)
    private Gender parseGender(String raw) {
        raw = safeTrim(raw);
        if (raw == null) return null;
        try {
            return Gender.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // 잘못된 값이면 null 반환
        }
    }

    // User 엔티티를 UserResponse DTO로 변환
    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getPhone(),
                u.getBirthDate(),
                u.getGender()
        );
    }
}