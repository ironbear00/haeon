package com.example.demo.service;

import com.example.demo.domain.Deceased;
import com.example.demo.repository.DeceasedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeceasedService {

    private final DeceasedRepository deceasedRepository;

    public List<Deceased> findMyDeceasedList(Long loggedInUserId) {
        return deceasedRepository.findByManagerUser_Id(loggedInUserId);
    }
}