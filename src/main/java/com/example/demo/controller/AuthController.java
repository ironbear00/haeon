package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // [유지] 프론트 경로와 일치
public class AuthController {

    public static final String SESSION_USER_ID = "LOGIN_USER_ID"; // [유지]

    private final AuthService userService;

    public AuthController(AuthService userService) { // [유지]
        this.userService = userService;
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest req) {
        UserResponse res = userService.signup(req);
        return ResponseEntity.status(201).body(res); // [유지]
    }

    // 로그인(세션)
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest req, HttpSession session) {
        User user = userService.authenticate(req);
        session.setAttribute(SESSION_USER_ID, user.getId()); // [유지]

        // [수정] UserResponse 생성자 시그니처 변경(6개 인자 모두 전달)
        UserResponse res = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),       // [수정] 추가
                user.getBirthDate(),   // [수정] 추가
                user.getGender()       // [수정] 추가
        );

        return ResponseEntity.ok(res); // [유지]
    }

    // 내 정보
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpSession session) {
        Object userIdAttr = session.getAttribute(SESSION_USER_ID);
        if (userIdAttr == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = (Long) userIdAttr;
        UserResponse me = userService.getProfile(userId);
        return ResponseEntity.ok(me);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}