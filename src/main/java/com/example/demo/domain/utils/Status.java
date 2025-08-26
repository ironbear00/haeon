package com.example.demo.domain.utils;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.GenerationType.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "status")
public class Status {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // SUBMITTED, PROCESSING, COMPLETED, REJECTED, DELETED, CANCELLED 등

    @Column(length = 255)
    private String description;
}
