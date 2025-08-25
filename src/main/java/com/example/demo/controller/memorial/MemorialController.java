package com.example.demo.controller.memorial;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/memorial")
public class MemorialController {

    @GetMapping("")
    public String memorial(){
        return "memorial";
    }

    @GetMapping("/memorial_detail")
    public String memorialDetail(){
        return "memorial_detail";
    }

    @GetMapping("/memorial_write")
    public String memorialWrite(){
        return "memorial_write";
    }
}
