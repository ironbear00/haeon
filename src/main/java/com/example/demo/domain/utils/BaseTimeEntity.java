package com.example.demo.domain.utils;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseTimeEntity {

    @Column(name = "create_at", nullable = false, updatable = false) // [수정] 컬럼명 명시
    private LocalDateTime createAt;

    @Column(name = "update_at", nullable = false)                    // [수정] 컬럼명 명시
    private LocalDateTime updateAt;

    @PrePersist
    protected void onCreate() {
        this.createAt = LocalDateTime.now(); // [유지]
        this.updateAt = LocalDateTime.now(); // [유지]
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = LocalDateTime.now(); // [유지]
    }
}