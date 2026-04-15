package com.campusconnect.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MessageDeleteDTO {
    private Long messageId;
    private Long deletedBy;
    private Long otherUserId;
    private String deleteType;
}