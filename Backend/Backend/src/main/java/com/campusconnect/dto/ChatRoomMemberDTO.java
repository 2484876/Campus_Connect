package com.campusconnect.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoomMemberDTO {
    private Long userId;
    private String name;
    private String profilePicUrl;
    private String position;
    private String role;
    private String presence;
}