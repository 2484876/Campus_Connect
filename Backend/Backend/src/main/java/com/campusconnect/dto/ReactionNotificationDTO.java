package com.campusconnect.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReactionNotificationDTO {
    private Long messageId;
    private Long userId;
    private String userName;
    private String emoji;
    private String action;
}