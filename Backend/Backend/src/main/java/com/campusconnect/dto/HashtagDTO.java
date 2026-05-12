package com.campusconnect.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HashtagDTO {
    private Long id;
    private String tag;
    private Long usageCount;
}