package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReadReceiptDTO {
    private Long readByUserId;
    private Long senderUserId;
    private List<Long> messageIds;
    private LocalDateTime readAt;
}