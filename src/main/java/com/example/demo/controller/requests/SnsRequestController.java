package com.example.demo.controller.requests;

import com.example.demo.dto.requests.SnsRequestDTO;
import com.example.demo.service.Requests.SnsRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
public class SnsRequestController {

    private final SnsRequestService snsRequestService;

    @GetMapping("/dashboard")
    public String requestsDashboardPage(){
        return "dashboard";
    }

    @GetMapping("/apply")
    public String requestsApplyPage(){
        return "apply";
    }


    @PostMapping("/apply")
    @ResponseBody
    public ResponseEntity<Void> createSnsRequest(
            @ModelAttribute SnsRequestDTO requestDto,
            HttpSession session) {

        // ★ 수정: 하드코딩된 ID 대신 세션에서 실제 사용자 ID를 가져옵니다.
        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        if (userId == null) {
            return ResponseEntity.status(401).build(); // 로그인되지 않은 경우
        }

        try {
            snsRequestService.createSnsRequests(userId, requestDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}