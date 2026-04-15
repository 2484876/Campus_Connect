package com.campusconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommunityPost {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String videoUrl;

    private int upvotes = 0;
    private int downvotes = 0;
    private boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;
}