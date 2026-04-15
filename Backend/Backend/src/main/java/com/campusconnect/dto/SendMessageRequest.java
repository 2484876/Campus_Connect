package com.campusconnect.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotNull
    private Long receiverId;

    @NotBlank
    private String content;

    private Long replyToId;
}