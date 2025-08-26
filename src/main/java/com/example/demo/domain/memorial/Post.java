package com.example.demo.domain.memorial;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.User;
import com.example.demo.domain.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "post")
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deceased_id", nullable = false)
    private Deceased deceased;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String deceasedPhotoPath;

    @Column(nullable = false, length = 50)
    private String status = "SUBMITTED";

    @Column(nullable = false, unique = true, length = 100)
    private String uuidLink = UUID.randomUUID().toString();

    @Column(nullable = false, length = 255)
    private String accessPassword;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;
}