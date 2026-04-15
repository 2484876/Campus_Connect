package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateExperienceRequest {
    @NotBlank
    private String title;
    private String employmentType;
    private String company;
    private String location;
    @NotNull
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;
    private String description;
}