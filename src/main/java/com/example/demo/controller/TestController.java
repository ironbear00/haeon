
package com.example.demo.controller;

import com.example.demo.domain.requests.UserResponse;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.controller.AuthController.SESSION_USER_ID;

@RestController
@RequestMapping("/api")
public class TestController {

    private final UserService userService;

    public TestController(UserService userService) {
        this.userService = userService;
    }

    // 로그인된 사용자만 접근 가능한 핑 API
    @GetMapping("/ping")
    public ResponseEntity<UserResponse> ping(HttpSession session) {
        Object id = session.getAttribute(SESSION_USER_ID);
        if (id == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = (Long) id;
        // 현재 로그인된 사용자 정보 반환
        return ResponseEntity.ok(userService.getProfile(userId)); // [추가]
    }
}