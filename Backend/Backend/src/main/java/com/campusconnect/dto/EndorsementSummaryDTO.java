package com.campusconnect.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EndorsementSummaryDTO {
    private long totalReceived;
    private long totalGiven;
    /** {skill: "Java", count: 8} - top skills by endorsement count */
    private List<SkillCount> topSkills;
    /** {skill: "TECHNICAL", count: 15} - category breakdown */
    private List<SkillCount> categoryBreakdown;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SkillCount {
        private String label;
        private long count;
    }
}