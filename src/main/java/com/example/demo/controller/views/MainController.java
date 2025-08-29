package com.example.demo.controller.views;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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