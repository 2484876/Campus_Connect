package com.campusconnect.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttachmentDTO {
    private Long id;
    private String attachmentType;
    private String url;
    private String fileName;
    private Long fileSize;
    private Integer durationSeconds;
}