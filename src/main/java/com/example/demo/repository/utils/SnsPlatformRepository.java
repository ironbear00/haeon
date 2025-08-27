package com.example.demo.repository.utils;

import com.example.demo.domain.utils.SnsPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SnsPlatformRepository extends JpaRepository<SnsPlatform, Long> {
    boolean existsByName(String name);
    Optional<SnsPlatform> findByName(String name);
}
