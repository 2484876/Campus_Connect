package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommunityRequest {
    @NotBlank @Size(max = 100)
    private String name;
    private String description;
    private String iconUrl;
    private String bannerUrl;
    private boolean isPrivate;
}