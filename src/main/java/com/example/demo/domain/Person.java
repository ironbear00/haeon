package com.example.demo.domain;

import com.example.demo.domain.utils.Gender;
import com.example.demo.domain.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@Table(name = "person")
public abstract class Person extends BaseTimeEntity {

    @Column(nullable = false, length = 100)
    private String name;

    private String phone;
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;
}