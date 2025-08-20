package com.example.demo.repository;

import com.example.demo.domain.SnsPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnsPlatformRepository extends JpaRepository<SnsPlatform, Long> {
    boolean existsByName(String name);
}
