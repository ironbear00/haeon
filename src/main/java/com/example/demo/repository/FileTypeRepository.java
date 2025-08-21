package com.example.demo.repository;

import com.example.demo.domain.utils.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileTypeRepository extends JpaRepository<FileType, Long> {
    boolean existsByCode(String code);
}
