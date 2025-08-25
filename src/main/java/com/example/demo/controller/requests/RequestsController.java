package com.example.demo.controller.requests;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/requests")
public class RequestsController {

    @GetMapping("/dashboard")
    public String requestsDashboardPage(){
        return "requests_dashboard";
    }

    @GetMapping("/apply")
    public String requestsApplyPage(){ return "apply";}
}
