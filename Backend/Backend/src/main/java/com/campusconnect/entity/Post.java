package com.campusconnect.entity;

import com.campusconnect.enums.PostType;
import com.campusconnect.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String imageUrl;
    @Column(length = 500)
    private String videoUrl;
    @Enumerated(EnumType.STRING)
    private PostType postType = PostType.GENERAL;

    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PUBLIC;

    private boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}