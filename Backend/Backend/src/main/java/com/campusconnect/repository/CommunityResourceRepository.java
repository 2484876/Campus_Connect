package com.campusconnect.repository;

import com.campusconnect.entity.CommunityResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommunityResourceRepository extends JpaRepository<CommunityResource, Long> {

    @Query("SELECT r FROM CommunityResource r WHERE r.community.id = :cid ORDER BY r.createdAt DESC")
    Page<CommunityResource> findByCommunity(Long cid, Pageable pageable);

    @Query("SELECT r FROM CommunityResource r WHERE r.community.id = :cid AND (LOWER(r.title) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(r.tags) LIKE LOWER(CONCAT('%',:q,'%'))) ORDER BY r.createdAt DESC")
    Page<CommunityResource> searchByCommunity(Long cid, String q, Pageable pageable);
}