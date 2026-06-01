package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PresenceDTO {
    private Long userId;
    private String status;
    private LocalDateTime lastSeen;
}