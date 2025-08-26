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

        List<RequestFile> savedFiles = new ArrayList<>();
        Map<String, FileType> fileTypes = Map.of(
                "RELATION_CERTIFICATION", fileTypeRepository.findByCode("RELATION_CERTIFICATION").orElseThrow(),
                "DEATH_CERTIFICATE", fileTypeRepository.findByCode("DEATH_CERTIFICATE").orElseThrow(),
                "APPLICANT_ID", fileTypeRepository.findByCode("APPLICANT_ID").orElseThrow()
        );

        String relationCertPath = fileStore.storeFile(dto.getRelationCertification());
        RequestFile relationFile = new RequestFile();
        relationFile.setFilePath(relationCertPath);
        relationFile.setFileType(fileTypes.get("RELATION_CERTIFICATION"));
        savedFiles.add(requestFileRepository.save(relationFile));

        String deathCertPath = fileStore.storeFile(dto.getDeathCertificate());
        RequestFile deathFile = new RequestFile();
        deathFile.setFilePath(deathCertPath);
        deathFile.setFileType(fileTypes.get("DEATH_CERTIFICATE"));
        savedFiles.add(requestFileRepository.save(deathFile));

        String applicantIdPath = fileStore.storeFile(dto.getApplicantId());
        RequestFile applicantFile = new RequestFile();
        applicantFile.setFilePath(applicantIdPath);
        applicantFile.setFileType(fileTypes.get("APPLICANT_ID"));
        savedFiles.add(requestFileRepository.save(applicantFile));

        for (String platformName : dto.getPlatforms()) {
            SnsPlatform platform = snsPlatformRepository.findByName(platformName)
                    .orElseThrow(() -> new IllegalArgumentException("플랫폼을 찾을 수 없습니다: " + platformName));

            SnsRequest snsRequest = new SnsRequest();
            snsRequest.setRequester(requester);
            snsRequest.setDeceased(deceased);
            snsRequest.setSnsPlatform(platform);

            for (RequestFile file : savedFiles) {
                snsRequest.addFile(file);
            }

            snsRequestRepository.save(snsRequest);
        }
    }
}
