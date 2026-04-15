package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommunityCommentRequest {
    @NotBlank
    private String content;
    private Long parentCommentId;
}