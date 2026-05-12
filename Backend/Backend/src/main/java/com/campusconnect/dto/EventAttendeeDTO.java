package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EventAttendeeDTO {
    private Long userId;
    private String name;
    private String profilePicUrl;
    private String position;
    private String department;
    private String role;
    private String status;
    private LocalDateTime rsvpAt;
}