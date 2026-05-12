package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AchievementDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String tier;
    private Integer points;
    private LocalDateTime earnedAt;
    private boolean earned;
}