package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SignupRequest {
    @JsonProperty("name")       // [추가]
    private String name;

    @JsonProperty("email")      // [추가]
    private String email;

    @JsonProperty("password")   // [추가]
    private String password;

    // 선택값
    @JsonProperty("phone")      // [추가]
    private String phone;

    // "YYYY-MM-DD"
    @JsonProperty("birthDate")  // [추가]
    private String birthDate;

    // "female" | "male" | "other"
    @JsonProperty("gender")     // [추가]
    private String gender;

}