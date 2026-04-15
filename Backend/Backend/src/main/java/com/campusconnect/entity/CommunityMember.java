package com.campusconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"community_id", "user_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommunityMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String role = "MEMBER";

    @CreationTimestamp
    private LocalDateTime joinedAt;
}