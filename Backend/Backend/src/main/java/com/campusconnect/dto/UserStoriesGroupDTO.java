package com.campusconnect.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserStoriesGroupDTO {
    private Long userId;
    private String userName;
    private String userProfilePic;
    private boolean allViewed;
    private List<StoryDTO> stories;
}