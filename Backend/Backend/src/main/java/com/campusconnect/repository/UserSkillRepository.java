package com.campusconnect.repository;
import com.campusconnect.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}