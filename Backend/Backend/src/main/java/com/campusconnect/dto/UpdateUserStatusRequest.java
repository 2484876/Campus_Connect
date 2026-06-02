package com.campusconnect.dto;

import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    private boolean active;
    private String reason;
}