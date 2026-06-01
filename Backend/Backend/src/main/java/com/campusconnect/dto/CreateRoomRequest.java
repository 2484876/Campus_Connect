package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateRoomRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String avatarUrl;

    @NotEmpty
    private List<Long> memberIds;
}