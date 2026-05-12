package com.campusconnect.repository;

import com.campusconnect.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PollRepository extends JpaRepository<Poll, Long> {
    Optional<Poll> findByPostId(Long postId);
}