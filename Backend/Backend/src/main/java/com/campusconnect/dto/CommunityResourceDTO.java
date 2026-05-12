package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommunityResourceDTO {
    private Long id;
    private Long communityId;
    private Long uploadedById;
    private String uploadedByName;
    private String uploadedByProfilePic;
    private String title;
    private String description;
    private String resourceType;
    private String url;
    private Long fileSizeBytes;
    private String mimeType;
    private String tags;
    private int clickCount;
    private LocalDateTime createdAt;
}