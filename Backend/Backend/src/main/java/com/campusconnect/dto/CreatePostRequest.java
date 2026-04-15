package com.campusconnect.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class CreatePostRequest {
    @NotBlank @Size(max = 5000) private String content;
    private String imageUrl;
    private String videoUrl;
    private String postType;
}