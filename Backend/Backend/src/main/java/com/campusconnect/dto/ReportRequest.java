package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReportRequest {
    @NotBlank
    private String targetType;
    private Long targetId;
    @NotBlank
    private String reason;
    private String details;
}