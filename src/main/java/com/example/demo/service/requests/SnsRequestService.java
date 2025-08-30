package com.example.demo.service.requests;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.User;
import com.example.demo.domain.requests.RequestFile;
import com.example.demo.domain.requests.SnsRequest;
import com.example.demo.domain.utils.FileType;
import com.example.demo.domain.utils.SnsPlatform;
import com.example.demo.dto.DeceasedRequest;
import com.example.demo.dto.DeceasedResponse;
import com.example.demo.dto.requests.SnsRequestDTO;
import com.example.demo.dto.requests.SnsRequestSummaryDTO;
import com.example.demo.repository.DeceasedRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.requests.SnsRequestRepository;
import com.example.demo.repository.utils.FileTypeRepository;
import com.example.demo.repository.utils.SnsPlatformRepository;
import com.example.demo.service.utils.FileStore;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SnsRequestService {
    private final SnsRequestRepository snsRequestRepository;
    private final UserRepository userRepository;
    private final FileTypeRepository fileTypeRepository;
    private final SnsPlatformRepository snsPlatformRepository;
    private final DeceasedRepository deceasedRepository;
    private final FileStore fileStore;
    private final RequestProcessingService requestProcessingService;

    public void createSnsRequests(Long userId, SnsRequestDTO dto) throws IOException {

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long selectedDeceasedId = dto.getDeceasedId();
        if (selectedDeceasedId == null) {
            throw new IllegalArgumentException("요청할 고인 ID가 없습니다.");
        }

        Deceased deceased = deceasedRepository.findByIdAndManagerUserId(selectedDeceasedId, requester.getId())
                .orElseThrow(() -> new IllegalArgumentException("요청한 고인 정보를 찾을 수 없거나 접근 권한이 없습니다."));

        String relationCertPath = fileStore.storeFile(dto.getRelationCertification());
        String deathCertPath = fileStore.storeFile(dto.getDeathCertificate());
        String applicantIdPath = fileStore.storeFile(dto.getApplicantId());

        Supplier<IllegalArgumentException> notFound = () -> new IllegalArgumentException("FileType 코드를 찾을 수 없습니다.");
        FileType relationType = fileTypeRepository.findByCode("RELATION_CERTIFICATION").orElseThrow(notFound);
        FileType deathType = fileTypeRepository.findByCode("DEATH_CERTIFICATE").orElseThrow(notFound);
        FileType applicantIdType = fileTypeRepository.findByCode("APPLICANT_ID").orElseThrow(notFound);

        for (String platformName : dto.getPlatforms()) {
            SnsPlatform platform = snsPlatformRepository.findByName(platformName)
                    .orElseThrow(() -> new IllegalArgumentException("플랫폼을 찾을 수 없습니다: " + platformName));

            SnsRequest snsRequest = SnsRequest.builder()
                    .requester(requester)
                    .deceased(deceased)
                    .snsPlatform(platform)
                    .reason(dto.getReason())
                    .build();

            snsRequest.addFile(createRequestFile(relationCertPath, relationType));
            snsRequest.addFile(createRequestFile(deathCertPath, deathType));
            snsRequest.addFile(createRequestFile(applicantIdPath, applicantIdType));

            SnsRequest savedRequest = snsRequestRepository.save(snsRequest);
            requestProcessingService.processSnsRequest(savedRequest.getId());
        }
    }

    private RequestFile createRequestFile(String filePath, FileType fileType) {
        RequestFile requestFile = new RequestFile();
        requestFile.setFilePath(filePath);
        requestFile.setFileType(fileType);
        return requestFile;
    }

    @Transactional(readOnly = true)
    public List<SnsRequestSummaryDTO> findMyRequests(Long userId) {
        return snsRequestRepository.findByRequester_IdOrderByCreateAtDesc(userId)
                .stream()
                .map(SnsRequestSummaryDTO::new)
                .collect(Collectors.toList());
    }
}