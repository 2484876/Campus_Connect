package com.campusconnect.repository;

import com.campusconnect.entity.StoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface StoryViewRepository extends JpaRepository<StoryView, Long> {
    boolean existsByStoryIdAndViewerId(Long storyId, Long viewerId);
    long countByStoryId(Long storyId);
    List<StoryView> findByStoryIdOrderByViewedAtDesc(Long storyId);

    /** Delete all views for a specific story (used when manually deleting a story). */
    @Modifying
    @Query("DELETE FROM StoryView v WHERE v.story.id = :storyId")
    int deleteByStoryId(Long storyId);

    /** Delete all views whose parent story has expired. Called before deleting the stories themselves. */
    @Modifying
    @Query("DELETE FROM StoryView v WHERE v.story.id IN (SELECT s.id FROM Story s WHERE s.expiresAt < :now)")
    int deleteViewsOfExpiredStories(LocalDateTime now);
}