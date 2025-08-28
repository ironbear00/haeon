package com.example.demo.service.requests.processors;

import com.example.demo.domain.requests.SnsRequest;
import com.example.demo.domain.utils.SnsPlatform;
import com.example.demo.repository.requests.SnsRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetaProcessor implements SnsPlatformProcessor {

    private final SnsRequestRepository snsRequestRepository;

    @Override
    public boolean supports(SnsPlatform platform) {
        return "Meta".equalsIgnoreCase(platform.getName());
    }

    @Override
    public void process(SnsRequest request) {
        try {
            log.info("[Meta] 요청 처리 시작: ID {}", request.getId());
            request.setStatus("PROCESSING");
            snsRequestRepository.save(request);

            log.info("[Meta] 글로벌 서버 연결 중...");
            Thread.sleep(4000); // 4초

            // Meta는 사망진단서를 필수로 확인한다고 가정
            boolean hasDeathCert = request.getFiles().stream()
                    .anyMatch(file -> "DEATH_CERTIFICATE".equals(file.getFileType().getCode()));

            if (!hasDeathCert) {
                log.error("[Meta] 필수 서류(사망진단서) 누락. 요청 거절.");
                request.setStatus("REJECTED");
                snsRequestRepository.save(request);
                return;
            }

            log.info("[Meta] 서류 확인 완료. APAC 담당팀 검토 대기...");
            Thread.sleep(7000); // 7초

            // 20% 확률로 특별한 이유 없이 거절될 수 있다고 가정
            if (ThreadLocalRandom.current().nextInt(10) < 2) { // 0, 1
                log.warn("[Meta] 정책상의 이유로 요청이 거절되었습니다.");
                request.setStatus("REJECTED");
            } else {
                request.setStatus("COMPLETED");
            }

            snsRequestRepository.save(request);
            log.info("[Meta] 처리 완료: ID {}", request.getId());

        } catch (InterruptedException e) {
            log.error("[Meta] 처리 중 오류 발생", e);
            request.setStatus("REJECTED");
            snsRequestRepository.save(request);
            Thread.currentThread().interrupt();
        }
    }
}