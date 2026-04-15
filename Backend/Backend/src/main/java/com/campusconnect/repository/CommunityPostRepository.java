package com.campusconnect.repository;

import com.campusconnect.entity.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    @Query("SELECT p FROM CommunityPost p WHERE p.community.id = :cid AND p.isActive = true ORDER BY p.createdAt DESC")
    Page<CommunityPost> findByCommunityId(@Param("cid") Long communityId, Pageable pageable);

    @Query("SELECT p FROM CommunityPost p WHERE p.isActive = true AND p.community.id IN (SELECT cm.community.id FROM CommunityMember cm WHERE cm.user.id = :uid) ORDER BY p.createdAt DESC")
    Page<CommunityPost> findFeedForUser(@Param("uid") Long userId, Pageable pageable);
}