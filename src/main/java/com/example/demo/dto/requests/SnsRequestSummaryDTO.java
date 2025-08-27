package com.example.demo.dto.requests;

import com.example.demo.domain.requests.SnsRequest;
import lombok.Getter;
import java.time.format.DateTimeFormatter;

@Getter
public class SnsRequestSummaryDTO {
    private final String platformName;
    private final String status;
    private final String createdAt;

    public SnsRequestSummaryDTO(SnsRequest snsRequest) {
        this.platformName = snsRequest.getSnsPlatform().getName();
        this.status = snsRequest.getStatus(); // 상태 Enum 등으로 변환 가능
        this.createdAt = snsRequest.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}
