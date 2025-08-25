package com.example.demo.controller;

import com.example.demo.dto.UserResponse;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.controller.UserController.SESSION_USER_ID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final UserService userService; // AuthService -> UserService로 변경

    @GetMapping("/ping")
    public ResponseEntity<UserResponse> ping(HttpSession session) {
        Object id = session.getAttribute(SESSION_USER_ID);
        if (id == null) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized 반환
        }
        Long userId = (Long) id;
        return ResponseEntity.ok(userService.getProfile(userId));
    }
}