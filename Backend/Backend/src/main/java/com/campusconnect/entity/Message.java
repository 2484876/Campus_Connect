package com.campusconnect.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_msg_sender_receiver", columnList = "sender_id, receiver_id"),
        @Index(name = "idx_msg_receiver_read", columnList = "receiver_id, read_status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "read_status", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean readStatus = false;

    private LocalDateTime readAt;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted = false;

    private Long deletedBy;

    private LocalDateTime deletedAt;

    @Column(name = "delete_type")
    private String deleteType;

    @Column(name = "hidden_for")
    private Long hiddenFor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MessageReaction> reactions = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
}