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
@AttributeOverrides({
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
    private AuthProvider provider=AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @OneToMany(mappedBy = "managerUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deceased> deceasedList = new ArrayList<>();
}