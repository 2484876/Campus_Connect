package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private LocalDateTime eventEndDate;
    private String location;
    private String imageUrl;
    private String coverUrl;
    private String eventType;
    private String category;
    private String virtualLink;
    private Integer maxParticipants;
    private boolean showAttendees;

    private Long createdById;
    private String createdByName;
    private String createdByProfilePic;
    private String createdByRole;
    private LocalDateTime createdAt;

    private long goingCount;
    private long interestedCount;
    private String myRsvpStatus;

    private boolean isPast;
    private boolean isLive;
}