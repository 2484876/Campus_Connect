package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ExperienceDTO {
    private Long id;
    private String title;
    private String employmentType;
    private String company;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;
    private String description;
}

