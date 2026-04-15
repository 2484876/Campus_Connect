package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommunityPostRequest {
    @NotBlank
    private String content;
    private String imageUrl;
    private String videoUrl;
}