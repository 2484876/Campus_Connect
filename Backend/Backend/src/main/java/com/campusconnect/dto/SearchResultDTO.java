package com.campusconnect.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SearchResultDTO {
    private List<UserDTO> users;
    private List<PostDTO> posts;
    private List<CommunityDTO> communities;
    private List<EventDTO> events;
    private List<HashtagDTO> hashtags;
}