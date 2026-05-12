package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RsvpRequest {
    @NotBlank
    private String status;
}