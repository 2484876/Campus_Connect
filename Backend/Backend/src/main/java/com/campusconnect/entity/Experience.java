package com.campusconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "experiences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Experience {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 100)
    private String employmentType;

    @Column(length = 200)
    private String company;

    @Column(length = 200)
    private String location;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private boolean isCurrent = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;
}