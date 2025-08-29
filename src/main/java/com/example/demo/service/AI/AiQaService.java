package com.example.demo.service.AI;

import com.example.demo.client.LlmClient;
import org.springframework.stereotype.Service;

@Service
public class AiQaService {

    private final LlmClient llmClient;

    public AiQaService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    /**
     * 항상 간단 요약 모드 강제:
     * - 3~4줄 이내 / 300~400자 내 / 불릿 최대 3개
     * - 불필요한 서론·주의 문구 금지
     */
    private static final String BASE_SYSTEM =
            "너는 장례/의전/추모 관련 질문에 답하는 비서야. " +
                    "항상 간단하고 핵심만, 3~4줄 이내 또는 불릿 3개 이내로 답변해. " +
                    "불필요한 서론·경고·법률자문 문구는 빼고, 실무적으로 바로 쓸 수 있게 요약해. " +
                    "가능하면 단계(1, 2, 3)나 간단 불릿으로 정리하고, 총 300~400자 이내로 제한해.";

    public String ask(String system, String question) {
        // 전달된 system이 있으면 덧붙이고, 없으면 BASE_SYSTEM만 사용
        String effectiveSystem = (system != null && !system.isBlank())
                ? (system.trim() + " " + BASE_SYSTEM)
                : BASE_SYSTEM;

        StringBuilder sb = new StringBuilder();
        sb.append("[시스템 지시]\n").append(effectiveSystem).append("\n\n");

        if (question != null && !question.isBlank()) {
            sb.append("[사용자 질문]\n").append(question.trim());
        } else {
            sb.append("[사용자 질문]\n").append("간단히 자기소개 해줘.");
        }

        String prompt = sb.toString();
        return llmClient.chat(prompt);
    }
}