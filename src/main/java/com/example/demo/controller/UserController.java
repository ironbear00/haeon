package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    public static final String SESSION_USER_ID = "LOGIN_USER_ID";

    private final UserService userService;

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest req) {
        UserResponse res = userService.signup(req);
        return ResponseEntity.status(201).body(res);
    }

    /** 로그인 (SecurityContext + 세션 저장) */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest req,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        // 1) 사용자 인증 (아이디/비번 검증)
        User user = userService.authenticate(req);

        // 2) 권한 부여
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // 3) principal 생성 (username은 템플릿에서 #authentication.name 으로 사용 가능)
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("")   // 이미 검증 완료 → 빈 문자열
                .authorities(authorities)
                .build();

        // 4) Authentication 생성 후 SecurityContext에 저장
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 5) 세션에 SecurityContext 보존 (다음 요청에서도 로그인 유지)
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);

        // (선택) 사용자 id도 세션에 저장
        request.getSession(true).setAttribute(SESSION_USER_ID, user.getId());

        // 6) 응답
        return ResponseEntity.ok(toResponse(user));
    }

    /** 로그인 상태 확인 (개발 편의용)
     *  GET /user/auth/status → { loggedIn: true/false, userId: 123 or null }
     */
    @GetMapping("/auth/status")
    public ResponseEntity<Map<String, Object>> authStatus(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        Object userId = session.getAttribute(SESSION_USER_ID); // 세션의 로그인 사용자 ID
        boolean loggedIn = (userId != null);
        res.put("loggedIn", loggedIn);            // 로그인 여부
        res.put("userId", loggedIn ? userId : null); // 로그인 시에만 사용자 ID 반환
        return ResponseEntity.ok(res);
    }

    /** 내 정보 조회 */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object userIdAttr = (session != null) ? session.getAttribute(SESSION_USER_ID) : null;
        if (userIdAttr == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = (Long) userIdAttr;
        UserResponse me = userService.getProfile(userId);
        return ResponseEntity.ok(me);
    }

    /** User → DTO 변환 */
    private static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getBirthDate(),
                user.getGender()
        );
    }
}