package com.campusconnect.entity;

import com.campusconnect.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_msg_sender_receiver", columnList = "sender_id, receiver_id"),
        @Index(name = "idx_msg_receiver_read", columnList = "receiver_id, read_status"),
        @Index(name = "idx_msg_chat_room", columnList = "chat_room_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType = MessageType.TEXT;

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

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean edited = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MessageReaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MessageAttachment> attachments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
}