package com.campusconnect.dto;

import lombok.Data;

@Data
public class CreateCommunityPostRequest {
    private String content;
    private String imageUrl;
    private String videoUrl;
    private String postType;
    private boolean anonymous;
}