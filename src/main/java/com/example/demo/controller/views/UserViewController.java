package com.example.demo.controller.views;

import com.example.demo.controller.UserController;
import jakarta.servlet.http.HttpServletRequest;
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
    @GetMapping("/mypage")
    public String mypagePage(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserController.SESSION_USER_ID) == null) {
            return "redirect:/user/login";
        }
        return "mypage";
    }

}