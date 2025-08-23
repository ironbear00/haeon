package com.example.demo.domain;

import com.example.demo.domain.utils.AuthProvider;
import com.example.demo.domain.utils.AuthProviderAttributeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "users")
@AttributeOverrides({ // 상속 필드 컬럼명 매핑 (name/phone/birthDate/gender)
        @AttributeOverride(name = "name",      column = @Column(name = "name",       nullable = false, length = 100)),
        @AttributeOverride(name = "phone",     column = @Column(name = "phone",      length = 255)),
        @AttributeOverride(name = "birthDate", column = @Column(name = "birth_date")),
        @AttributeOverride(name = "gender",    column = @Column(name = "gender",     length = 16))
})
public class User extends Person {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Convert(converter = AuthProviderAttributeConverter.class)
    @Column(name = "provider", length = 20, nullable = false)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @OneToMany(mappedBy = "managerUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deceased> deceasedList = new ArrayList<>();

    // [수정] provider가 null로 저장되는 것 방지 (LOCAL 기본값 등)
    private void applyDefaults() { // [수정]
        if (this.provider == null) {
            this.provider = AuthProvider.LOCAL; // [수정] 프로젝트 기본 정책에 맞게 변경 가능
        }
    }
}