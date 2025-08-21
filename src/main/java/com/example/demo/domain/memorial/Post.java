package com.example.demo.domain.memorial;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.User;
import com.example.demo.domain.utils.BaseTimeEntity;
import com.example.demo.domain.utils.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private Status status;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments=new ArrayList<>();

    @Column(nullable = false, unique = true, length = 100)
    private String uuidLink= UUID.randomUUID().toString();

    @Column(nullable = false, length = 255)
    private String accessPassword;

    public void addCommnet(Comment comment)
    {
        comments.add(comment);
        comment.setPost(this);
    }
}
