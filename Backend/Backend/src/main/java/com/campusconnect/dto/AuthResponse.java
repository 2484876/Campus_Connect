package com.campusconnect.dto;
import lombok.*;
@Data @Builder @AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private String role;
}