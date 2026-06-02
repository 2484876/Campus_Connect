package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminActionLogDTO {
    private Long id;
    private Long adminId;
    private String adminName;
    private String action;
    private String targetType;
    private Long targetId;
    private String targetLabel;
    private String details;
    private LocalDateTime createdAt;
}