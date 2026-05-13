package com.campusconnect.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MutualConnectionDTO {
    private Long userId;
    private String name;
    private String profilePicUrl;
    private String position;
    private String department;
}