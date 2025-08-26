package com.example.demo.domain.requests;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.User;
import com.example.demo.domain.utils.BaseTimeEntity;
import com.example.demo.domain.utils.SnsPlatform;
import com.example.demo.domain.utils.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "sns_requests")
public class SnsRequest extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deceased_id", nullable = false)
    private Deceased deceased;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private SnsPlatform snsPlatform;

    @Column(nullable = false, length = 50)
    private String status = "SUBMITTED"; // 기본값

    @OneToMany(mappedBy = "snsRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestFile> files=new ArrayList<>();

    @Column(length = 1000)
    private String reason;

    public void addFile(RequestFile file)
    {
        files.add(file);
        file.setSnsRequest(this);
    }
}
