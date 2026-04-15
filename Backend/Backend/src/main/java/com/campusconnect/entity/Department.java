package com.campusconnect.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hod_user_id")
    private User hod;
}
