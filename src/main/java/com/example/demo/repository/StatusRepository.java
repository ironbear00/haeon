package com.example.demo.repository;

import com.example.demo.domain.utils.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    boolean existsByCode(String code);
    Optional<Status> findByCode(String code);
}
