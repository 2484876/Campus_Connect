package com.campusconnect.dto;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConversationDTO {
    private Long userId;
    private String userName;
    private String userProfilePic;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
}