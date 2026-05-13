package com.campusconnect.entity;

import com.campusconnect.enums.EndorsementCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "skill_endorsements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"endorser_id", "endorsee_id", "skill", "category"}),
        indexes = {
                @Index(name = "idx_endorsee", columnList = "endorsee_id"),
                @Index(name = "idx_endorsee_skill", columnList = "endorsee_id, skill"),
                @Index(name = "idx_endorsee_cat", columnList = "endorsee_id, category")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillEndorsement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endorser_id", nullable = false)
    private User endorser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endorsee_id", nullable = false)
    private User endorsee;

    @Column(length = 60)
    private String skill;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EndorsementCategory category;

    @Column(length = 300)
    private String message;

    @CreationTimestamp
    private LocalDateTime createdAt;
}