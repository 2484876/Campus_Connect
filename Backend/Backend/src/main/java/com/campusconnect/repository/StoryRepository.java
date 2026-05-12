package com.campusconnect.repository;

import com.campusconnect.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {

    @Query("SELECT s FROM Story s WHERE s.expiresAt > :now AND (s.user.id = :uid OR s.user.id IN (SELECT CASE WHEN c.sender.id = :uid THEN c.receiver.id ELSE c.sender.id END FROM Connection c WHERE (c.sender.id = :uid OR c.receiver.id = :uid) AND c.status = 'ACCEPTED')) ORDER BY s.createdAt DESC")
    List<Story> findActiveForUser(Long uid, LocalDateTime now);

    @Query("SELECT s FROM Story s WHERE s.user.id = :uid AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveByUserId(Long uid, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Story s WHERE s.expiresAt < :now")
    int deleteExpired(LocalDateTime now);
}