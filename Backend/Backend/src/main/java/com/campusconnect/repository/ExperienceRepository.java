package com.campusconnect.repository;

import com.campusconnect.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    @Query("SELECT e FROM Experience e WHERE e.user.id = :uid ORDER BY e.isCurrent DESC, e.startDate DESC")
    List<Experience> findByUserId(@Param("uid") Long userId);
}