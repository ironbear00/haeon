package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.domain.requests.LoginRequest;                 // [수정] 로그인에 필요
import com.example.demo.domain.requests.SignupRequest;
import com.example.demo.domain.requests.UserResponse;
import com.example.demo.domain.utils.AuthProvider;                   // [수정] provider 기본값 처리
import com.example.demo.domain.utils.Gender;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // [유지]

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) { // [유지]
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder; // [유지]
    }

    /**
     * 회원가입
     * - SignupRequest는 phone/birthDate/gender를 문자열로 받는다.
     * - 여기서 LocalDate/Enum으로 변환 후 User 엔티티에 세팅한다.
     * - 형식 오류/빈값은 null 처리(필요시 기본값으로 바꿔도 됨).
     */
    @Transactional
    public UserResponse signup(SignupRequest req) { // [유지]
        // [수정] 이메일 정규화(공백 제거 + 소문자)
        String email = normalizeEmail(req.getEmail());                             // [수정]
        if (email == null) {
            throw new IllegalArgumentException("이메일은 필수입니다.");                // [수정]
        }

        // [수정] 이메일 중복 체크
        Optional<User> existing = userRepository.findByEmail(email);               // [수정]
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");        // [유지]
        }

        // [수정] 이름/비밀번호 필수 검증
        String name = safeTrim(req.getName());                                     // [수정]
        if (name == null) {
            throw new IllegalArgumentException("이름은 필수입니다.");                  // [수정]
        }
        String rawPw = req.getPassword();                                          // [유지]
        if (rawPw == null || rawPw.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");               // [유지]
        }

        User user = new User();
        user.setName(name);                                                        // [유지]
        user.setEmail(email);                                                      // [수정]
        user.setPassword(passwordEncoder.encode(rawPw));                           // [유지]

        // phone 그대로(또는 숫자만 남기기)
        String phone = safeTrim(req.getPhone());                                   // [유지]
        // phone = phone != null ? phone.replaceAll("-", "") : null;              // 숫자만 원하면 주석 해제
        user.setPhone(emptyToNull(phone));                                         // [유지]

        // birthDate: "yyyy-MM-dd" → LocalDate
        LocalDate birth = parseBirthDate(req.getBirthDate());                      // [유지]
        user.setBirthDate(birth);                                                  // [유지]

        // gender: "female/male/other"(대소문자 무관) → Gender enum
        Gender gender = parseGender(req.getGender());                              // [유지]
        user.setGender(gender);                                                    // [유지]

        // [수정] provider 기본값(LOCAL) 강제 설정 (엔티티 @PrePersist 있어도 이중 안전장치)
        if (user.getProvider() == null) {                                          // [수정]
            user.setProvider(AuthProvider.LOCAL);                                  // [수정]
        }

        User saved = userRepository.save(user);                                    // [유지]
        return toResponse(saved);                                                  // [유지]
    }

    /**
     * 로그인 인증 (세션 설정은 컨트롤러)
     */
    @Transactional(readOnly = true)                                                // [수정]
    public User authenticate(LoginRequest req) {                                    // [수정]
        String email = normalizeEmail(req.getEmail());                              // [수정]
        if (email == null || req.getPassword() == null) {
            throw new IllegalArgumentException("이메일/비밀번호를 확인하세요.");          // [수정]
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다.")); // [수정]

        if (user.getPassword() == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) { // [수정]
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");          // [수정]
        }
        return user;                                                                // [수정]
    }

    /**
     * 내 프로필 조회
     */
    @Transactional(readOnly = true)                                                // [수정]
    public UserResponse getProfile(Long userId) {                                   // [수정]
        if (userId == null) throw new IllegalArgumentException("userId는 필수입니다.");  // [수정]
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")); // [수정]
        return toResponse(user);                                                    // [수정]
    }

    // ==========================
    // 내부 유틸 메서드
    // ==========================

    // 안전 trim
    private String safeTrim(String s) {                                             // [유지]
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // 빈문자열 → null
    private String emptyToNull(String s) {                                          // [유지]
        return (s == null || s.isBlank()) ? null : s;
    }

    // 이메일 정규화
    private String normalizeEmail(String raw) {                                     // [수정]
        String t = safeTrim(raw);
        return t == null ? null : t.toLowerCase();
    }

    // "yyyy-MM-dd" 파싱 (실패 시 null)
    private LocalDate parseBirthDate(String raw) {                                  // [유지]
        raw = safeTrim(raw);
        if (raw == null) return null;
        try {
            return LocalDate.parse(raw); // ISO(yyyy-MM-dd)
        } catch (DateTimeParseException e) {
            return null; // 형식이 다르면 null 저장(또는 예외로 변경 가능)
        }
    }

    // 문자열 → Gender enum (대소문자 무관, 틀리면 null)
    private Gender parseGender(String raw) {                                        // [유지]
        raw = safeTrim(raw);
        if (raw == null) return null;
        try {
            return Gender.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // 잘못된 값이면 null (또는 Gender.OTHER로 강제)
            // return Gender.OTHER;
        }
    }

    // 엔티티 → 응답 DTO
    private UserResponse toResponse(User u) {                                       // [유지]
        UserResponse res = new UserResponse();
        res.setId(u.getId());
        res.setName(u.getName());
        res.setEmail(u.getEmail());
        res.setPhone(u.getPhone());
        res.setBirthDate(u.getBirthDate()); // LocalDate 그대로 (필요시 문자열 포맷으로 바꿔도 됨)
        res.setGender(u.getGender());       // Enum 그대로 (필요시 .name() 또는 한글 변환)
        return res;
    }
}