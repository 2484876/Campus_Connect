package com.campusconnect.repository;

import com.campusconnect.entity.CommunityMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {
    Optional<CommunityMember> findByCommunityIdAndUserId(Long communityId, Long userId);
    boolean existsByCommunityIdAndUserId(Long communityId, Long userId);

    @Query("SELECT cm FROM CommunityMember cm WHERE cm.community.id = :cid ORDER BY cm.joinedAt DESC")
    Page<CommunityMember> findByCommunityId(@Param("cid") Long communityId, Pageable pageable);

    void deleteByCommunityIdAndUserId(Long communityId, Long userId);
}