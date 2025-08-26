package com.example.demo.service.Requests;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.User;
import com.example.demo.domain.requests.RequestFile;
import com.example.demo.domain.requests.SnsRequest;
import com.example.demo.domain.utils.FileType;
import com.example.demo.domain.utils.SnsPlatform;
import com.example.demo.dto.requests.SnsRequestDTO;
import com.example.demo.repository.DeceasedRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.requests.RequestFileRepository;
import com.example.demo.repository.requests.SnsRequestRepository;
import com.example.demo.repository.utils.FileTypeRepository;
import com.example.demo.repository.utils.SnsPlatformRepository;
import com.example.demo.service.utils.FileStore;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional
public class SnsRequestService {
    private final SnsRequestRepository snsRequestRepository;
    private final RequestFileRepository requestFileRepository;
    private final UserRepository userRepository;
    private final FileTypeRepository fileTypeRepository;
    private final SnsPlatformRepository snsPlatformRepository;
    private final DeceasedRepository deceasedRepository;
    private final FileStore fileStore;

    public void createSnsRequests(Long userId, SnsRequestDTO dto) throws IOException {

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Deceased> deceasedList = deceasedRepository.findByManagerUser_Id(requester.getId());
        if (deceasedList.isEmpty()) {
            throw new IllegalStateException("관리하는 고인 정보가 등록되어 있지 않습니다. 먼저 고인 정보를 등록해주세요.");
        }
        Deceased deceased = deceasedList.get(0);

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

            SnsRequest snsRequest = new SnsRequest();
            snsRequest.setRequester(requester);
            snsRequest.setDeceased(deceased);
            snsRequest.setSnsPlatform(platform);

            snsRequest.addFile(createRequestFile(relationCertPath, relationType));
            snsRequest.addFile(createRequestFile(deathCertPath, deathType));
            snsRequest.addFile(createRequestFile(applicantIdPath, applicantIdType));

            snsRequestRepository.save(snsRequest);
        }
    }


    private RequestFile createRequestFile(String filePath, FileType fileType) {
        RequestFile requestFile = new RequestFile();
        requestFile.setFilePath(filePath);
        requestFile.setFileType(fileType);
        return requestFile;
    }
}
