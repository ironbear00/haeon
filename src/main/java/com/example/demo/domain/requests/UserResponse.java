package com.example.demo.domain.requests;

import com.example.demo.domain.utils.Gender;                 // [수정]
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;                             // [수정]
import lombok.Setter;                                        // [수정]

import java.time.LocalDate;                                  // [수정]

@Getter
@Setter                                                     // [수정] toResponse()에서 set* 사용 가능하도록 추가
@NoArgsConstructor                                           // [수정]
@AllArgsConstructor
public class UserResponse {

    private Long id;                                         // [유지]
    private String name;                                     // [유지]
    private String email;                                    // [유지]

    // ---- 아래 필드 추가 ----
    private String phone;                                    // [수정] 추가
    private LocalDate birthDate;                             // [수정] 추가
    private Gender gender;                                   // [수정] 추가
}