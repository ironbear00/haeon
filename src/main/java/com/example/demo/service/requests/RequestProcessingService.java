package com.example.demo.service.requests;

import com.example.demo.domain.requests.SnsRequest;
import com.example.demo.repository.requests.SnsRequestRepository;
import com.example.demo.service.requests.processors.SnsPlatformProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestProcessingService {

    private final SnsRequestRepository snsRequestRepository;
    private final List<SnsPlatformProcessor> processors; // ★ 모든 Processor 구현체를 주입받음

    @Async
    @Transactional
    public void processSnsRequest(Long requestId) {
        SnsRequest request = snsRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다: " + requestId));

        processors.stream()
                .filter(processor -> processor.supports(request.getSnsPlatform()))
                .findFirst()
                .ifPresentOrElse(
                        processor -> processor.process(request),
                        () -> {
                            log.error("요청 ID {}: 지원하는 Processor를 찾을 수 없습니다.", requestId);
                            request.setStatus("REJECTED");
                            snsRequestRepository.save(request);
                        }
                );
    }
}