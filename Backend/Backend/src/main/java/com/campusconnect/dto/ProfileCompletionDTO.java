package com.campusconnect.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfileCompletionDTO {
    private int percent;          // 0-100
    private List<MissingField> missing;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MissingField {
        private String key;       // "bio", "avatar", "experience", "skills", "phone", "banner", "birthday"
        private String label;     // human-readable
        private int weight;       // how many percentage points it's worth
    }
}