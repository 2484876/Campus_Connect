package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReactionDTO {
    private Long id;
    private Long messageId;
    private Long userId;
    private String userName;
    private String emoji;
    private LocalDateTime createdAt;
}