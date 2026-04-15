package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteMessageRequest {
    @NotNull
    private Long messageId;

    @NotBlank
    private String deleteType;
}