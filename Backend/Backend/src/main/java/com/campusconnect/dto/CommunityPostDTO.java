package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommunityPostDTO {
    private Long id;
    private Long communityId;
    private String communityName;
    private String communityIconUrl;
    private Long authorId;
    private String authorName;
    private String authorProfilePic;
    private String authorRole;
    private String content;
    private String imageUrl;
    private String videoUrl;
    private int upvotes;
    private int downvotes;
    private int score;
    private int userVote;
    private int commentCount;
    private LocalDateTime createdAt;
}