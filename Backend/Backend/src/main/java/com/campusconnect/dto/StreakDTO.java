package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StreakDTO {
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActiveDate;
    private int totalCheckIns;
    private boolean checkedInToday;
}