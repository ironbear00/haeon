package com.example.demo.repository;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeceasedRepository extends JpaRepository<Deceased, Long> {
    List<Deceased> findByManagerUser_Id(Long UserId);

}
