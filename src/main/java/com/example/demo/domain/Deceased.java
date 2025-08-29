package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@SuperBuilder
@Table(name = "deceased")
public class Deceased extends Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_user_id", nullable = false)
    private User managerUser;

    private LocalDate deathDate;
    private LocalDate funeralDate;
}
