package com.campusconnect.repository;

import com.campusconnect.entity.StoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoryViewRepository extends JpaRepository<StoryView, Long> {
    boolean existsByStoryIdAndViewerId(Long storyId, Long viewerId);
    long countByStoryId(Long storyId);
    List<StoryView> findByStoryIdOrderByViewedAtDesc(Long storyId);
}