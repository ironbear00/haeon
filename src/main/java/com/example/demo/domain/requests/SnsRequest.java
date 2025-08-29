package com.example.demo.domain.requests;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.User;
import com.example.demo.domain.utils.BaseTimeEntity;
import com.example.demo.domain.utils.SnsPlatform;
import com.example.demo.domain.utils.Status;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "sns_requests")
@SuperBuilder
@NoArgsConstructor
public class SnsRequest extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "deceased_id", nullable = false)
    private Deceased deceased;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private SnsPlatform snsPlatform;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "SUBMITTED";

    @OneToMany(mappedBy = "snsRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequestFile> files=new ArrayList<>();

    @Column(length = 1000)
    private String reason;

    public void addFile(RequestFile file)
    {
        files.add(file);
        file.setSnsRequest(this);
    }
}
