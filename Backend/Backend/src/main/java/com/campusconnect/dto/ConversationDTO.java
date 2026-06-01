package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConversationDTO {
    private String kind;
    private Long userId;
    private Long roomId;
    private String userName;
    private String userProfilePic;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
    private String presence;
    private int memberCount;
}