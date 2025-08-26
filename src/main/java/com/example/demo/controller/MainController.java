package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class MainController {

    // 로그인 페이지를 반환하는 GET 메서드 추가
    @GetMapping("/user/login")
    public String loginPage() {
        return "login"; // templates/login.html 파일을 찾아서 반환
    }

    @GetMapping({"/", "main"})
    public String mainPage() {
        return "main";
    }

    @GetMapping("/process")
    public String processPage() {
        return "process";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }
}