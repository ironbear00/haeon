package com.example.demo.repository.requests;

import com.example.demo.domain.requests.SnsRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnsRequestRepository extends JpaRepository<SnsRequest,Long> {
}
