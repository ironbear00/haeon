package com.example.demo.service.requests.processors;

import com.example.demo.domain.requests.SnsRequest;
import com.example.demo.domain.utils.SnsPlatform;
import com.example.demo.repository.requests.SnsRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverProcessor implements SnsPlatformProcessor {

    private final SnsRequestRepository snsRequestRepository;

    @Override
    public boolean supports(SnsPlatform platform) {
        return "Naver".equalsIgnoreCase(platform.getName());
    }

    @Override
    public void process(SnsRequest request) {
        try {
            log.info("[Naver] 요청 처리 시작: ID {}", request.getId());
            request.setStatus("PROCESSING");
            snsRequestRepository.save(request);

            log.info("[Naver] 네이버 인증 시스템 연결...");
            Thread.sleep(2500); // 2.5초

            // 네이버는 모든 서류를 필수로 확인한다고 가정
            boolean hasAllDocs = request.getFiles().stream()
                    .filter(file -> List.of("DEATH_CERTIFICATE", "RELATION_CERTIFICATION", "APPLICANT_ID")
                            .contains(file.getFileType().getCode()))
                    .count() == 3;

            if (!hasAllDocs) {
                log.error("[Naver] 필수 서류 3종 누락. 요청 거절.");
                request.setStatus("REJECTED");
                snsRequestRepository.save(request);
                return;
            }

            log.info("[Naver] 모든 서류 확인 완료. 내부 심사를 시작합니다...");
            Thread.sleep(12000); // 12초 (가장 김)

            request.setStatus("COMPLETED");
            snsRequestRepository.save(request);
            log.info("[Naver] 처리 완료: ID {}", request.getId());

        } catch (InterruptedException e) {
            log.error("[Naver] 처리 중 오류 발생", e);
            request.setStatus("REJECTED");
            snsRequestRepository.save(request);
            Thread.currentThread().interrupt();
        }
    }
}