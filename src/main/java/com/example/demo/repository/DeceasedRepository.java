package com.example.demo.repository;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeceasedRepository extends JpaRepository<Deceased, Long> {
    List<Deceased> findByManagerUser_Id(Long managerUserId);
    Optional<Deceased> findByIdAndManagerUserId(Long deceasedId, Long managerUserId);
}
