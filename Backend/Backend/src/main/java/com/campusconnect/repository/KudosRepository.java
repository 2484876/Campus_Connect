package com.campusconnect.repository;

import com.campusconnect.entity.Kudos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KudosRepository extends JpaRepository<Kudos, Long> {

    @Query("SELECT k FROM Kudos k WHERE k.receiver.id = :userId AND k.isPublic = true ORDER BY k.createdAt DESC")
    Page<Kudos> findReceivedPublic(Long userId, Pageable pageable);

    @Query("SELECT k FROM Kudos k WHERE k.giver.id = :userId ORDER BY k.createdAt DESC")
    Page<Kudos> findGiven(Long userId, Pageable pageable);

    @Query("SELECT k FROM Kudos k WHERE k.isPublic = true ORDER BY k.createdAt DESC")
    Page<Kudos> findRecentPublic(Pageable pageable);

    long countByReceiverIdAndIsPublicTrue(Long receiverId);
    long countByGiverId(Long giverId);
}