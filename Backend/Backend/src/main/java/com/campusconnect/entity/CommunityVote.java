package com.campusconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id", "comment_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommunityVote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private CommunityComment comment;

    private int value;

    @CreationTimestamp
    private LocalDateTime createdAt;
}