package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SkillEndorsementDTO {
    private Long id;
    private Long endorserId;
    private String endorserName;
    private String endorserProfilePic;
    private String endorserPosition;
    private Long endorseeId;
    private String endorseeName;
    private String skill;          // null when category-only
    private String category;       // null when skill-only
    private String message;
    private LocalDateTime createdAt;
}