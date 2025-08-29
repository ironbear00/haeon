package com.example.demo.controller.AI;

import com.example.demo.service.AI.AiQaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiQaController {

    private final AiQaService aiQaService;

    public AiQaController(AiQaService aiQaService) {
        this.aiQaService = aiQaService;
    }

    @PostMapping("/ask")
    public String ask(@RequestBody String q) {
        // 기존: aiQaService.answer(q)
        // 변경: aiQaService.ask(null, q)  // system 프롬프트는 없으므로 null 전달
        String answer = aiQaService.ask(null, q);
        return answer;
    }
}