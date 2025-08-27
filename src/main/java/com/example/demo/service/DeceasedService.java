package com.example.demo.service;

import com.example.demo.domain.Deceased;
<<<<<<< HEAD
import com.example.demo.domain.User;
import com.example.demo.dto.DeceasedRequest;
import com.example.demo.dto.DeceasedResponse;
import com.example.demo.repository.DeceasedRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
=======
import com.example.demo.repository.DeceasedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
>>>>>>> master

import java.util.List;

@Service
@RequiredArgsConstructor
<<<<<<< HEAD
@Transactional
public class DeceasedService {

    private final DeceasedRepository deceasedRepository;
    private final UserRepository userRepository;

    @Transactional
    public DeceasedResponse createDeceased(Long userId, DeceasedRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Deceased deceased = new Deceased();
        deceased.setName(req.getName());
        deceased.setPhone(req.getPhone());
        deceased.setBirthDate(req.getBirthDate());
        deceased.setGender(req.getGender());
        deceased.setDeathDate(req.getDeathDate());
        deceased.setFuneralDate(req.getFuneralDate());
        deceased.setManagerUser(user);

        Deceased saved = deceasedRepository.save(deceased);
        return deceasedResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DeceasedResponse> getDeceasedList(Long userId) {
        return deceasedRepository.findByManagerUser_Id(userId)
                .stream()
                .map(this::deceasedResponse)
                .toList();
    }

    // ✅ 단건 조회 (수정 화면 등에 사용)
    @Transactional(readOnly = true)
    public DeceasedResponse getDeceasedById(Long deceasedId) {
        Deceased d = deceasedRepository.findById(deceasedId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고인입니다."));
        return deceasedResponse(d);
    }

    @Transactional
    public DeceasedResponse updateDeceased(Long deceasedId, DeceasedRequest req) {
        Deceased deceased = deceasedRepository.findById(deceasedId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고인입니다."));

        deceased.setName(req.getName());
        deceased.setPhone(req.getPhone());
        deceased.setBirthDate(req.getBirthDate());
        deceased.setGender(req.getGender());
        deceased.setDeathDate(req.getDeathDate());
        deceased.setFuneralDate(req.getFuneralDate());

        Deceased updated = deceasedRepository.save(deceased);
        return deceasedResponse(updated);
    }

    @Transactional
    public void deleteDeceased(Long deceasedId) {
        deceasedRepository.deleteById(deceasedId);
    }

    // 공통 변환 메서드
    private DeceasedResponse deceasedResponse(Deceased d) {
        return new DeceasedResponse(
                d.getId(),
                d.getName(),
                d.getPhone(),
                d.getBirthDate(),
                d.getGender(),
                d.getDeathDate(),
                d.getFuneralDate()
        );
    }
}
=======
public class DeceasedService {

    private final DeceasedRepository deceasedRepository;

    public List<Deceased> findMyDeceasedList(Long loggedInUserId) {
        return deceasedRepository.findByManagerUser_Id(loggedInUserId);
    }
}
>>>>>>> master
