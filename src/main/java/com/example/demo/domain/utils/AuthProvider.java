package com.example.demo.domain.utils;

import com.fasterxml.jackson.annotation.JsonCreator;   // [유지]
import com.fasterxml.jackson.annotation.JsonValue;    // [유지]
import java.util.Locale;                              // [유지]

/**
 * DB ENUM: 'google','kakao','local','meta','naver'  (소문자)
 * 자바 ENUM: 상수는 대문자, JSON/JPA 저장은 소문자 문자열로.
 */
public enum AuthProvider {

    GOOGLE("google"),
    KAKAO("kakao"),
    LOCAL("local"),
    META("meta"),
    NAVER("naver");

    private final String dbValue;

    AuthProvider(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() { return dbValue; }

    @JsonCreator
    public static AuthProvider from(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase(Locale.ROOT);
        for (AuthProvider p : values()) {
            if (p.dbValue.equals(v)) return p;
        }
        throw new IllegalArgumentException("Unknown provider: " + value);
    }

    // [삭제] 이너 클래스 Converter 제거 → 이름 충돌 원인 해소
}