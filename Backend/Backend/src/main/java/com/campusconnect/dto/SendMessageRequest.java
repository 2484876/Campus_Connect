package com.campusconnect.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class SendMessageRequest {
    private Long receiverId;

    private Long chatRoomId;

    private String content;

    private String messageType;

    private Long replyToId;

    private List<AttachmentDTO> attachments;
}