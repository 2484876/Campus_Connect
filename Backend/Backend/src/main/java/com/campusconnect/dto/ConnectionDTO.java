package com.campusconnect.dto;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConnectionDTO {
    private Long connectionId;
    private Long userId;
    private String userName;
    private String userProfilePic;
    private String userPosition;
    private String userRole;
    private String userDepartment;
    private String status;
    private LocalDateTime createdAt;
}