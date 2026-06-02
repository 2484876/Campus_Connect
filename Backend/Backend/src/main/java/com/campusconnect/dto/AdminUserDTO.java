package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminUserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String department;
    private String position;
    private String profilePicUrl;
    private boolean active;
    private LocalDateTime createdAt;
    private long postCount;
}