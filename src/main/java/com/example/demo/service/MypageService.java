package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return userResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userid, UpdateUserRequest req) {
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }
        if (req.getBirthDate() != null) {
            try {
                user.setBirthDate(LocalDate.parse(req.getBirthDate())); // "yyyy-MM-dd" 형식
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다. (yyyy-MM-dd)");
            }
        }

        User saved = userRepository.save(user);
        return userResponse(saved);
    }


    private UserResponse userResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getPhone(),
                u.getBirthDate(),
                u.getGender()
        );
    }
}
