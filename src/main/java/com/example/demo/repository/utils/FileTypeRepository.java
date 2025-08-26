package com.example.demo.repository.utils;

import com.example.demo.domain.utils.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileTypeRepository extends JpaRepository<FileType, Long> {
    boolean existsByCode(String code);
    Optional<FileType> findByCode(String code);
}
