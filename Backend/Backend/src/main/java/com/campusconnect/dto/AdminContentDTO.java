package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminContentDTO {
    private Long id;
    private String type;
    private Long authorId;
    private String authorName;
    private String preview;
    private boolean active;
    private LocalDateTime createdAt;
}