package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateResourceRequest {
    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String resourceType;

    @NotBlank
    private String url;

    private Long fileSizeBytes;
    private String mimeType;
    private String tags;
}