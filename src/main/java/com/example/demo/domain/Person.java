package com.example.demo.domain;

import com.example.demo.domain.utils.Gender;
import com.example.demo.domain.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public abstract class Person extends BaseTimeEntity {

    @Column(nullable = false, length = 100)
    private String name;

    // 전화번호는 선택 값 (NULL 허용)
    @Column(name = "phone", length = 255)
    private String phone;

    // [수정] LocalDate로 명확하게 보관 (형식 변환은 Service에서 수행)
    @Column(name = "birth_date")
    private LocalDate birthDate;

    // [수정] Enum을 STRING으로 저장해 DB 가독성 및 호환성 확보
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 16)
    private Gender gender;
}