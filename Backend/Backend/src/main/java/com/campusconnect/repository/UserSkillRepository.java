package com.campusconnect.repository;

import com.campusconnect.entity.UserSkill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUserId(Long userId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM UserSkill us WHERE us.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT us.user.id FROM UserSkill us " +
            "WHERE LOWER(us.skillName) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Long> findUserIdsBySkill(@Param("q") String skillQuery, Pageable pageable);

    @Query("SELECT us.skillName, COUNT(us) FROM UserSkill us " +
            "WHERE LOWER(us.skillName) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "GROUP BY us.skillName ORDER BY COUNT(us) DESC")
    List<Object[]> searchSkillNames(@Param("q") String skillQuery, Pageable pageable);

    @Query("SELECT us.skillName, COUNT(us) FROM UserSkill us " +
            "GROUP BY us.skillName ORDER BY COUNT(us) DESC")
    List<Object[]> findTrendingSkills(Pageable pageable);
}