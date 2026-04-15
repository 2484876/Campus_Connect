package com.campusconnect.dto;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String type;
    private Long actorId;
    private String actorName;
    private String actorProfilePic;
    private Long referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String message;
}