package com.example.demo.domain.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 자바 enum <-> DB 소문자 ENUM 문자열 변환기
 */
@Converter(autoApply = false) // [중요] 사용하는 필드에 @Convert로 명시 적용
public class AuthProviderAttributeConverter implements AttributeConverter<AuthProvider, String> {

    @Override
    public String convertToDatabaseColumn(AuthProvider attribute) {
        return attribute == null ? null : attribute.getDbValue(); // DB에는 소문자 저장
    }

    @Override
    public AuthProvider convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AuthProvider.from(dbData); // 소문자 → enum
    }
}