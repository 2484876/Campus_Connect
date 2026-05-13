package com.campusconnect.repository;

import com.campusconnect.entity.UserSkill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    /** Find users who have a given skill (case-insensitive contains match). */
    @Query("SELECT DISTINCT us.user.id FROM UserSkill us " +
            "WHERE LOWER(us.skillName) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Long> findUserIdsBySkill(@Param("q") String skillQuery, Pageable pageable);

    /** All skills matching a query, with count (for autocomplete / trending). */
    @Query("SELECT us.skillName, COUNT(us) FROM UserSkill us " +
            "WHERE LOWER(us.skillName) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "GROUP BY us.skillName ORDER BY COUNT(us) DESC")
    List<Object[]> searchSkillNames(@Param("q") String skillQuery, Pageable pageable);

    /** Top trending skills across the org. */
    @Query("SELECT us.skillName, COUNT(us) FROM UserSkill us " +
            "GROUP BY us.skillName ORDER BY COUNT(us) DESC")
    List<Object[]> findTrendingSkills(Pageable pageable);
}