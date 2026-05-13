package com.campusconnect.repository;

import com.campusconnect.entity.SkillEndorsement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillEndorsementRepository extends JpaRepository<SkillEndorsement, Long> {

    Page<SkillEndorsement> findByEndorseeIdOrderByCreatedAtDesc(Long endorseeId, Pageable pageable);

    List<SkillEndorsement> findByEndorseeIdAndEndorserId(Long endorseeId, Long endorserId);

    @Query("SELECT e FROM SkillEndorsement e WHERE e.endorsee.id = :endorseeId AND e.endorser.id = :endorserId AND " +
            "( (:skill IS NULL AND e.skill IS NULL) OR e.skill = :skill ) AND " +
            "( (:cat IS NULL AND e.category IS NULL) OR e.category = :cat )")
    Optional<SkillEndorsement> findExactMatch(@Param("endorseeId") Long endorseeId,
                                              @Param("endorserId") Long endorserId,
                                              @Param("skill") String skill,
                                              @Param("cat") com.campusconnect.enums.EndorsementCategory cat);

    @Query("SELECT e.skill, COUNT(e) FROM SkillEndorsement e WHERE e.endorsee.id = :endorseeId AND e.skill IS NOT NULL GROUP BY e.skill ORDER BY COUNT(e) DESC")
    List<Object[]> countSkillsForUser(@Param("endorseeId") Long endorseeId);

    @Query("SELECT e.category, COUNT(e) FROM SkillEndorsement e WHERE e.endorsee.id = :endorseeId AND e.category IS NOT NULL GROUP BY e.category ORDER BY COUNT(e) DESC")
    List<Object[]> countCategoriesForUser(@Param("endorseeId") Long endorseeId);

    @Query("SELECT COUNT(e) FROM SkillEndorsement e WHERE e.endorsee.id = :endorseeId")
    long countByEndorseeId(@Param("endorseeId") Long endorseeId);

    @Query("SELECT COUNT(e) FROM SkillEndorsement e WHERE e.endorser.id = :endorserId")
    long countByEndorserId(@Param("endorserId") Long endorserId);

    @Query("SELECT e FROM SkillEndorsement e WHERE e.endorsee.id = :endorseeId AND e.skill = :skill ORDER BY e.createdAt DESC")
    List<SkillEndorsement> findEndorsersForSkill(@Param("endorseeId") Long endorseeId, @Param("skill") String skill);
}