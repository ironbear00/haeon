package com.example.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

    private final RestClient restClient;
    private final String apiKey;

    // application-*.properties 에서 ai.gemini.api-key 값을 주입
    public LlmClient(@Value("${ai.gemini.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    /**
     * Gemini 1.5 Flash 에 단문 질문 보내고 텍스트만 뽑아서 반환
     */
    public String chat(String prompt) {
        // 요청 바디 구성 (Jackson이 자동 직렬화)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", prompt)
                ))
        ));

        // 호출
        Map<?, ?> response = restClient.post()
                .uri("/models/gemini-1.5-flash:generateContent?key={key}", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        // 응답 파싱: candidates[0].content.parts[0].text
        if (response == null) return "(응답이 없습니다)";
        Object candidatesObj = response.get("candidates");
        if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) {
            return "(후보 응답이 없습니다)";
        }

        Object cand0 = candidates.get(0);
        if (!(cand0 instanceof Map<?, ?> candMap)) return "(응답 형식이 예상과 다릅니다)";

        Object contentObj = candMap.get("content");
        if (!(contentObj instanceof Map<?, ?> contentMap)) return "(content 없음)";

        Object partsObj = contentMap.get("parts");
        if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) return "(parts 없음)";

        Object part0 = parts.get(0);
        if (!(part0 instanceof Map<?, ?> partMap)) return "(part 형식 오류)";

        Object text = partMap.get("text");
        return text instanceof String s ? s : "(text 없음)";
    }
}