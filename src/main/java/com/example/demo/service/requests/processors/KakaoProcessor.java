package com.example.demo.service.requests.processors;

import com.example.demo.domain.requests.SnsRequest;
import com.example.demo.domain.utils.SnsPlatform;
import com.example.demo.repository.requests.SnsRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoProcessor implements SnsPlatformProcessor {

    private final SnsRequestRepository snsRequestRepository;

    @Override
    public boolean supports(SnsPlatform platform) {
        return "Kakao".equalsIgnoreCase(platform.getName());
    }

    @Override
    public void process(SnsRequest request) {
        try {
            log.info("[Kakao] 요청 처리 시작: ID {}", request.getId());
            request.setStatus("PROCESSING");
            snsRequestRepository.save(request);

            log.info("[Kakao] 카카오 서버 연결 시도...");
            Thread.sleep(2000); // 2초

            // 카카오는 신청인 신분증을 필수로 확인한다고 가정
            boolean hasApplicantId = request.getFiles().stream()
                    .anyMatch(file -> "APPLICANT_ID".equals(file.getFileType().getCode()));

            if (!hasApplicantId) {
                log.error("[Kakao] 필수 서류(신청인 신분증) 누락. 요청 거절.");
                request.setStatus("REJECTED");
                snsRequestRepository.save(request);
                return;
            }

            log.info("[Kakao] 서류 검토 중...");
            Thread.sleep(5000); // 5초

            request.setStatus("COMPLETED");
            snsRequestRepository.save(request);
            log.info("[Kakao] 처리 완료: ID {}", request.getId());

        } catch (InterruptedException e) {
            log.error("[Kakao] 처리 중 오류 발생", e);
            request.setStatus("REJECTED");
            snsRequestRepository.save(request);
            Thread.currentThread().interrupt();
        }
    }
}
