package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendEventChatRequest {
    @NotBlank
    @Size(max = 1000)
    private String content;
}