package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class MainController {
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
