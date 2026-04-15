package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommunityCommentDTO {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorName;
    private String authorProfilePic;
    private Long parentCommentId;
    private String content;
    private int upvotes;
    private int downvotes;
    private int score;
    private int userVote;
    private int replyCount;
    private List<CommunityCommentDTO> replies;
    private LocalDateTime createdAt;
}