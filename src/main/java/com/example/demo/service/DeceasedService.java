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

    public DeceasedResponse createDeceased(Long ownerUserId, DeceasedRequest req) {
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Deceased deceased = Deceased.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .birthDate(req.getBirthDate())
                .gender(req.getGender())
                .deathDate(req.getDeathDate())
                .funeralDate(req.getFuneralDate())
                .managerUser(owner)
                .build();

        Deceased saved = deceasedRepository.save(deceased);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DeceasedResponse> getDeceasedList(Long ownerUserId) {
        return deceasedRepository.findByManagerUser_Id(ownerUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Deceased> findMyDeceasedList(Long ownerUserId) {
        return deceasedRepository.findByManagerUser_Id(ownerUserId);
    }

    @Transactional(readOnly = true)
    public DeceasedResponse getDeceasedByIdForOwner(Long deceasedId, Long ownerUserId) {
        Deceased d = getOwnedEntity(deceasedId, ownerUserId);
        return toResponse(d);
    }

    public DeceasedResponse updateDeceasedForOwner(Long deceasedId, Long ownerUserId, DeceasedRequest req) {
        Deceased deceased = getOwnedEntity(deceasedId, ownerUserId);

        deceased.setName(req.getName());
        deceased.setPhone(req.getPhone());
        deceased.setBirthDate(req.getBirthDate());
        deceased.setGender(req.getGender());
        deceased.setDeathDate(req.getDeathDate());
        deceased.setFuneralDate(req.getFuneralDate());

        return toResponse(deceased);
    }

    public void deleteDeceasedForOwner(Long deceasedId, Long ownerUserId) {
        Deceased deceased = getOwnedEntity(deceasedId, ownerUserId);
        deceasedRepository.delete(deceased);
    }

    @Transactional(readOnly = true)
    public DeceasedResponse getDeceasedById(Long deceasedId) {
        Deceased d = deceasedRepository.findById(deceasedId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고인입니다."));
        return toResponse(d);
    }

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