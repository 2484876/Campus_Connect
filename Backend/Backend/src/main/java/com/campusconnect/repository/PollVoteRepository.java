package com.campusconnect.repository;

import com.campusconnect.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    List<PollVote> findByPollIdAndUserId(Long pollId, Long userId);
    Optional<PollVote> findByPollIdAndOptionIdAndUserId(Long pollId, Long optionId, Long userId);
    long countByPollIdAndOptionId(Long pollId, Long optionId);
    long countByPollId(Long pollId);

    @Query("SELECT COUNT(DISTINCT v.user.id) FROM PollVote v WHERE v.poll.id = :pollId")
    long countUniqueVoters(Long pollId);

    void deleteByPollIdAndOptionIdAndUserId(Long pollId, Long optionId, Long userId);
    void deleteByPollIdAndUserId(Long pollId, Long userId);
}