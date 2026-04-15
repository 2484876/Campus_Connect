package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderProfilePic;
    private Long receiverId;
    private String content;
    private boolean readStatus;
    private LocalDateTime readAt;
    private boolean deleted;
    private Long deletedBy;
    private String deleteType;
    private Long hiddenFor;
    private Long replyToId;
    private String replyToContent;
    private String replyToSenderName;
    private List<ReactionDTO> reactions;
    private LocalDateTime createdAt;
}