package com.campusconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pinned_messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PinnedMessage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false, unique = true)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pinned_by", nullable = false)
    private User pinnedBy;

    @Column(name = "chat_key", nullable = false, length = 100)
    private String chatKey;

    @CreationTimestamp
    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;
}