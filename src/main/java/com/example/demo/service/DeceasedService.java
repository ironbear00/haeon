package com.example.demo.service;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.User;
import com.example.demo.dto.DeceasedRequest;
import com.example.demo.dto.DeceasedResponse;
import com.example.demo.repository.DeceasedRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeceasedService {

    private final DeceasedRepository deceasedRepository;
    private final UserRepository userRepository;

    /* =========================
       생성
       ========================= */
    public DeceasedResponse createDeceased(Long ownerUserId, DeceasedRequest req) {
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Deceased deceased = new Deceased();
        deceased.setName(req.getName());
        deceased.setPhone(req.getPhone());
        deceased.setBirthDate(req.getBirthDate());
        deceased.setGender(req.getGender());
        deceased.setDeathDate(req.getDeathDate());
        deceased.setFuneralDate(req.getFuneralDate());
        deceased.setManagerUser(owner); // 소유자 설정

        Deceased saved = deceasedRepository.save(deceased);
        return toResponse(saved);
    }

    /* =========================
       목록 (내 소유만)
       ========================= */
    @Transactional(readOnly = true)
    public List<DeceasedResponse> getDeceasedList(Long ownerUserId) {
        return deceasedRepository.findByManagerUser_Id(ownerUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** 컨트롤러에서 셀렉트박스 등에 엔티티로 필요할 때 사용 (내 소유만 반환) */
    @Transactional(readOnly = true)
    public List<Deceased> findMyDeceasedList(Long ownerUserId) {
        return deceasedRepository.findByManagerUser_Id(ownerUserId);
    }

    /* =========================
       단건 조회 (내 소유 검증)
       ========================= */
    @Transactional(readOnly = true)
    public DeceasedResponse getDeceasedByIdForOwner(Long deceasedId, Long ownerUserId) {
        Deceased d = getOwnedEntity(deceasedId, ownerUserId);
        return toResponse(d);
    }

    /* =========================
       수정 (내 소유 검증)
       ========================= */
    public DeceasedResponse updateDeceasedForOwner(Long deceasedId, Long ownerUserId, DeceasedRequest req) {
        Deceased deceased = getOwnedEntity(deceasedId, ownerUserId);

        deceased.setName(req.getName());
        deceased.setPhone(req.getPhone());
        deceased.setBirthDate(req.getBirthDate());
        deceased.setGender(req.getGender());
        deceased.setDeathDate(req.getDeathDate());
        deceased.setFuneralDate(req.getFuneralDate());

        // JPA 변경감지로 flush 시 업데이트됨
        return toResponse(deceased);
    }

    /* =========================
       삭제 (내 소유 검증)
       ========================= */
    public void deleteDeceasedForOwner(Long deceasedId, Long ownerUserId) {
        Deceased deceased = getOwnedEntity(deceasedId, ownerUserId);
        deceasedRepository.delete(deceased);
    }

    /* =========================
       공용 도우미
       ========================= */
    @Transactional(readOnly = true)
    public DeceasedResponse getDeceasedById(Long deceasedId) {
        Deceased d = deceasedRepository.findById(deceasedId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고인입니다."));
        return toResponse(d);
    }

    /** 소유권 검증 포함 엔티티 조회 */
    @Transactional(readOnly = true)
    protected Deceased getOwnedEntity(Long deceasedId, Long ownerUserId) {
        Deceased d = deceasedRepository.findById(deceasedId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고인입니다."));
        if (d.getManagerUser() == null || d.getManagerUser().getId() == null
                || !d.getManagerUser().getId().equals(ownerUserId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다."); // 403에 해당
        }
        return d;
    }

    /** 엔티티 -> 응답 DTO 변환 */
    private DeceasedResponse toResponse(Deceased d) {
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