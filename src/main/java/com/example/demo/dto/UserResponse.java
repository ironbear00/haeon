package com.example.demo.dto;

import com.example.demo.domain.utils.Gender;                 // [수정]
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;                             // [수정]
import lombok.Setter;                                        // [수정]

import java.time.LocalDate;                                  // [수정]

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private Gender gender;
}