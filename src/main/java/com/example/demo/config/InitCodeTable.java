package com.example.demo.config;

import com.example.demo.domain.utils.FileType;
import com.example.demo.domain.utils.Status;
import com.example.demo.repository.FileTypeRepository;
import com.example.demo.repository.SnsPlatformRepository;
import com.example.demo.repository.StatusRepository;
import com.example.demo.domain.utils.SnsPlatform;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitCodeTable {

    //status code table initialization
    @Bean
    CommandLineRunner initStatus(StatusRepository statusRepository) {
        return args -> {
            String[][] statuses = {
                    {"SUBMITTED", "요청됨 / 게시글 작성됨"},
                    {"PROCESSING", "처리중"},
                    {"COMPLETED", "처리 완료"},
                    {"REJECTED", "반려됨"},
                    {"DELETED", "삭제됨"}, //삭제시 숨김처리 할 거임..
                    {"CANCELLED", "요청취소"}
            };

            for (String[] s : statuses) {
                String code = s[0];
                String desc = s[1];

                if (!statusRepository.existsByCode(code)) {
                    Status status = new Status();
                    status.setCode(code);
                    status.setDescription(desc);
                    statusRepository.save(status);
                    System.out.println("Status 추가: " + code);
                }
            }
        };
    }

    //platform code table initialization
    @Bean
    CommandLineRunner initPlatform(SnsPlatformRepository snsPlatformRepository) {
        return args -> {
            String[] platforms = {"GOOGLE", "NAVER", "KAKAO", "META"};

            for (String p : platforms) {
                if (!snsPlatformRepository.existsByName(p)) {
                    SnsPlatform platform = new SnsPlatform();
                    platform.setName(p);
                    snsPlatformRepository.save(platform);
                    System.out.println("Platform 추가: " + p);
                }
            }
        };
    }

    //initialization file type code table
    @Bean
    CommandLineRunner initFileType(FileTypeRepository fileTypeRepository) {
        return args -> {
            String[][] types = {
                    {"DEATH_CERTIFICATE", "사망증명서"},
                    {"APPLICANT_ID", "신청자 신분증"},
                    {"RELATION_CERTIFICATION", "관계 증명서"},
                    {"OTHER", "기타"}
            };

            for (String[] t : types) {
                if (!fileTypeRepository.existsByCode(t[0])) {
                    FileType ft = new FileType();
                    ft.setCode(t[0]);
                    ft.setName(t[1]);
                    fileTypeRepository.save(ft);
                }
            }
        };
    }
}
