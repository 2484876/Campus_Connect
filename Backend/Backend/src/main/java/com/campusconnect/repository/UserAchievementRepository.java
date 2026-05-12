package com.campusconnect.repository;

import com.campusconnect.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    boolean existsByUserIdAndAchievementCode(Long userId, String code);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user.id = :userId ORDER BY ua.earnedAt DESC")
    List<UserAchievement> findByUserIdOrdered(Long userId);

    long countByUserId(Long userId);
}