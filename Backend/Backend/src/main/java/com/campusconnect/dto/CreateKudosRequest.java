package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateKudosRequest {
    @NotNull
    private Long receiverId;
    @NotBlank
    private String category;
    private String message;
    private boolean isPublic = true;
}