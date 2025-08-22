package com.example.demo.domain.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    // --- getter / setter ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}