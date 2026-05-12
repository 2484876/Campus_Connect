package com.campusconnect.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateStoryRequest {
    private String mediaUrl;
    private String caption;
    private String backgroundColor;
}