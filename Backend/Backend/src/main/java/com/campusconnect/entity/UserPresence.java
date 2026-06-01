package com.campusconnect.entity;

import com.campusconnect.enums.PresenceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_presence")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserPresence {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PresenceStatus status = PresenceStatus.OFFLINE;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}