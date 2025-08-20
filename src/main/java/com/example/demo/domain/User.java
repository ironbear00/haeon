package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="users")
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @OneToMany(mappedBy = "managerUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deceased> deceasedList=new ArrayList<>();

    public void addDeceased(Deceased deceased){
        deceasedList.add(deceased);
        deceased.setManagerUser(this);
    }

    public void removeDeceased(Deceased deceased){
        deceasedList.remove(deceased);
        deceased.setManagerUser(null);
    }

}
