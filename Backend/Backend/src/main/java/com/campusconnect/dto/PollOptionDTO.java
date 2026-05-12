package com.campusconnect.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PollOptionDTO {
    private Long id;
    private String optionText;
    private long voteCount;
    private double percentage;
}