package com.campusconnect.repository;

import com.campusconnect.entity.CommunityVote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommunityVoteRepository extends JpaRepository<CommunityVote, Long> {
    Optional<CommunityVote> findByUserIdAndPostIdAndCommentIdIsNull(Long userId, Long postId);
    Optional<CommunityVote> findByUserIdAndCommentIdAndPostIdIsNull(Long userId, Long commentId);
}