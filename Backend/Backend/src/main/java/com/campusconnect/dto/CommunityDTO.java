package com.campusconnect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommunityDTO {
    private Long id;
    private String name;
    private String description;
    private String bannerUrl;
    private String iconUrl;
    private Long createdById;
    private String createdByName;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

    private int memberCount;

    @JsonProperty("isMember")
    private boolean isMember;

    private String memberRole;
    private LocalDateTime createdAt;
}