package com.campusconnect.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PollDTO {
    private Long id;
    private Long postId;
    private String question;
    private boolean multiChoice;
    private LocalDateTime expiresAt;
    private boolean expired;
    private long totalVotes;
    private List<PollOptionDTO> options;
    private boolean hasVoted;
    private List<Long> myVotedOptionIds;
}