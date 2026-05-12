package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateEventRequest {
    @NotBlank private String title;
    private String description;

    @NotNull private LocalDateTime eventDate;
    private LocalDateTime eventEndDate;

    private String location;
    private String imageUrl;
    private String coverUrl;

    private String eventType;
    private String category;
    private String virtualLink;

    private Integer maxParticipants;
    private Boolean showAttendees;
}