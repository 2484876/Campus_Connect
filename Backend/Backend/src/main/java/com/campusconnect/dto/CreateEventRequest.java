package com.campusconnect.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class CreateEventRequest {
    @NotBlank private String title;
    private String description;
    @NotNull private LocalDateTime eventDate;
    private String location;
    private String imageUrl;
    private Integer maxParticipants;
}