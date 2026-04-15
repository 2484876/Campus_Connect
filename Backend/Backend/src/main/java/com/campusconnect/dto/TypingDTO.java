package com.campusconnect.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TypingDTO {
    private Long userId;
    private String userName;
    private boolean typing;
}