package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreatePollRequest {
    @NotBlank
    private String question;
    private List<String> options;
    private boolean multiChoice;
    private LocalDateTime expiresAt;
}