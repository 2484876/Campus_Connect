package com.campusconnect.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConnectionSuggestionDTO {
    private Long userId;
    private String name;
    private String profilePicUrl;
    private String position;
    private String department;
    private String role;
    private int mutualCount;
    private String reason;        // "Same department · 3 mutual connections"
    private int score;            // for debugging / sort transparency
}