package com.campusconnect.dto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PostDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private String videoUrl;
    private String postType;
    private LocalDateTime createdAt;
    private Long authorId;
    private String authorName;
    private String authorProfilePic;
    private String authorPosition;
    private String authorRole;
    private String authorDepartment;
    private int likeCount;
    private int commentCount;
    private boolean likedByMe;
    private boolean bookmarkedByMe;
    private List<CommentDTO> recentComments;
}