package com.example.demo.controller.views;

import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.views.MypageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.controller.UserController.SESSION_USER_ID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage")
public class MypageController {

    private final MypageService mypageService;

@GetMapping
    public String mypage() {
        return "mypage";
    }

    @GetMapping("/info")
    public String mypageInfo() {
        return "mypage_info";
    }

    @GetMapping("/profile")
    @ResponseBody
    public UserResponse getProfile(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            throw new IllegalStateException("로그인 상태가 아닙니다.");
        }
        return mypageService.getProfile(userId);
    }

    @PutMapping("/profile")
    @ResponseBody
    public UserResponse updateProfile(HttpSession session, @RequestBody UpdateUserRequest req) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            throw new IllegalStateException("로그인 상태가 아닙니다.");
        }
        return mypageService.updateProfile(userId, req);
    }
}