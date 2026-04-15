package com.campusconnect.repository;

import com.campusconnect.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    @Query("SELECT c FROM Community c WHERE c.isActive = true ORDER BY c.memberCount DESC")
    Page<Community> findAllActive(Pageable pageable);

    @Query("SELECT c FROM Community c WHERE c.isActive = true AND (LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Community> search(@Param("q") String query, Pageable pageable);

    @Query("SELECT c FROM Community c JOIN CommunityMember cm ON cm.community.id = c.id WHERE cm.user.id = :uid AND c.isActive = true ORDER BY c.name ASC")
    Page<Community> findByMember(@Param("uid") Long userId, Pageable pageable);
}