package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoryDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userProfilePic;
    private String mediaUrl;
    private String caption;
    private String backgroundColor;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private long viewCount;
    private boolean viewedByMe;
}