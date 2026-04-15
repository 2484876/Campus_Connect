package com.campusconnect.dto;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorProfilePic;
    private LocalDateTime createdAt;
}