package com.example.demo.dto;

import com.example.demo.domain.utils.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeceasedResponse {
    private Long id;
    private String name;
    private String phone;
    private LocalDate birthDate;
    private Gender gender;

    private LocalDate deathDate;
    private LocalDate funeralDate;
}