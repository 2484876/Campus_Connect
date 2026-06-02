package com.campusconnect.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminStatsDTO {
    private long totalUsers;
    private long activeUsers;
    private long suspendedUsers;
    private long adminCount;
    private long totalPosts;
    private long activePosts;
    private long pendingReports;
    private long newUsersLast7Days;
}