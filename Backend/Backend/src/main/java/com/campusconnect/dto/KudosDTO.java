package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KudosDTO {
    private Long id;
    private Long giverId;
    private String giverName;
    private String giverProfilePic;
    private String giverPosition;
    private Long receiverId;
    private String receiverName;
    private String receiverProfilePic;
    private String receiverPosition;
    private String category;
    private String message;
    private boolean isPublic;
    private LocalDateTime createdAt;
}