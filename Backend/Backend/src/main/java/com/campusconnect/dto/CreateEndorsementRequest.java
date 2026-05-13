package com.campusconnect.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateEndorsementRequest {

    @NotNull
    private Long endorseeId;

    /** One of skill or category must be provided. */
    @Size(max = 60)
    private String skill;

    /** TECHNICAL, LEADERSHIP, TEAMWORK, COMMUNICATION, PROBLEM_SOLVING, MENTORSHIP */
    private String category;

    @Size(max = 300)
    private String message;
}