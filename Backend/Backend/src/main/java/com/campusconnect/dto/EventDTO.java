package com.campusconnect.dto;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private String imageUrl;
    private Integer maxParticipants;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}