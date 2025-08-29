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
public class GoogleProcessor implements SnsPlatformProcessor {

    private final SnsRequestRepository snsRequestRepository;

    @Override
    public boolean supports(SnsPlatform platform) {
        return "Google".equalsIgnoreCase(platform.getName());
    }

    @Override
    public void process(SnsRequest request) {
        try {
            log.info("[Google] 요청 처리 시작: ID {}", request.getId());

            // 1. 상태를 '처리 중'으로 변경
            request.setStatus("PROCESSING");
            snsRequestRepository.save(request);
            log.info("[Google] 상태 변경: PROCESSING");

            // 2. 구글 API에 연결하는 척 시뮬레이션
            log.info("[Google] 구글 본인 확인 시스템에 연결 시도...");
            Thread.sleep(3000);

            // 3. 서류(사망진단서) 유효성 검사하는 척 시뮬레이션
            boolean hasDeathCertificate = request.getFiles().stream()
                    .anyMatch(file -> "DEATH_CERTIFICATE".equals(file.getFileType().getCode()));

            if (!hasDeathCertificate) {
                log.error("[Google] 필수 서류(사망진단서) 누락. 요청 거절.");
                request.setStatus("REJECTED");
                snsRequestRepository.save(request);
                return;
            }
            log.info("[Google] 사망진단서 확인 완료.");
            Thread.sleep(2000);

            // 4. 구글 내부 검토팀의 검토를 기다리는 척 시뮬레이션
            log.info("[Google] 서류 제출 완료. 내부 검토를 기다립니다...");
            Thread.sleep(8000);

            // 5. 최종 처리 완료
            request.setStatus("COMPLETED");
            snsRequestRepository.save(request);
            log.info("[Google] 처리 완료: ID {}", request.getId());

        } catch (InterruptedException e) {
            log.error("[Google] 처리 중 오류 발생", e);
            request.setStatus("REJECTED");
            snsRequestRepository.save(request);
            Thread.currentThread().interrupt();
        }
    }
}