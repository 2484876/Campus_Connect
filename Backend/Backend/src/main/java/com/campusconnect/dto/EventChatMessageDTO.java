package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EventChatMessageDTO {
    private Long id;
    private Long eventId;
    private Long userId;
    private String userName;
    private String userProfilePic;
    private String userRole;
    private String content;
    private LocalDateTime createdAt;
}