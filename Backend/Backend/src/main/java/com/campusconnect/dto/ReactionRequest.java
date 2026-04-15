package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReactionRequest {
    @NotNull
    private Long messageId;

    @NotBlank
    private String emoji;
}