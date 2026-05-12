package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String department;
    private String position;
    private String bio;
    private String profilePicUrl;
    private String bannerUrl;
    private String phone;
    private LocalDate birthday;
    private LocalDate workAnniversary;
    private LocalDateTime createdAt;
    private List<String> skills;
    private int connectionCount;
    private String connectionStatus;
    private List<ExperienceDTO> experiences;
}