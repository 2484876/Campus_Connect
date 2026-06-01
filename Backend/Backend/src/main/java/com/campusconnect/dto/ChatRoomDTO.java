package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoomDTO {
    private Long id;
    private String name;
    private String avatarUrl;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
    private int memberCount;
    private String myRole;
    private List<ChatRoomMemberDTO> members;
}