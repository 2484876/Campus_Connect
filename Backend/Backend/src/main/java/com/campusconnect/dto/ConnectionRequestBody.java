package com.campusconnect.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ConnectionRequestBody {
    @NotNull
    private Long receiverId;

    @Size(max = 300)
    private String message;
}