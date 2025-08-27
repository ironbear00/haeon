package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserViewController {

    @GetMapping("/signup")
    public String signupPage() {
        // templates/signup.html 파일을 보여줌
        return "signup";
    }

    @GetMapping("/login")
    public String loginPage() {
        // templates/login.html 파일을 보여줌
        return "login";
    }
}