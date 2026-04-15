package com.campusconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "communities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Community {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String bannerUrl;

    @Column(length = 500)
    private String iconUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    private boolean isPrivate = false;
    private int memberCount = 1;
    private boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;
}