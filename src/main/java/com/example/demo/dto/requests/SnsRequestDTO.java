package com.example.demo.dto.requests;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class SnsRequestDTO {
    private List<String> platforms;
    private MultipartFile relationCertification;
    private MultipartFile deathCertificate;
    private MultipartFile applicantId;
    private List<MultipartFile> otherFiles;
}
