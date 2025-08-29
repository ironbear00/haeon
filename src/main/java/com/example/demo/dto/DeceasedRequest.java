package com.example.demo.dto;

import com.example.demo.domain.utils.Gender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DeceasedRequest {
    private String name;
    private String phone;
    private LocalDate birthDate;
    private Gender gender;

    private LocalDate deathDate;
    private LocalDate funeralDate;

}