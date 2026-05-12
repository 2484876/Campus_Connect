package com.campusconnect.repository;

import com.campusconnect.entity.EventRsvp;
import com.campusconnect.enums.RsvpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRsvpRepository extends JpaRepository<EventRsvp, Long> {

    Optional<EventRsvp> findByEventIdAndUserId(Long eventId, Long userId);

    long countByEventIdAndStatus(Long eventId, RsvpStatus status);

    @Query("SELECT r FROM EventRsvp r WHERE r.event.id = :eventId AND r.status = :status ORDER BY r.updatedAt DESC")
    List<EventRsvp> findByEventAndStatus(@Param("eventId") Long eventId, @Param("status") RsvpStatus status);

    @Query("SELECT r FROM EventRsvp r WHERE r.user.id = :userId AND r.status IN ('GOING','INTERESTED') ORDER BY r.event.eventDate ASC")
    List<EventRsvp> findMyUpcoming(@Param("userId") Long userId);

    boolean existsByEventIdAndUserIdAndStatus(Long eventId, Long userId, RsvpStatus status);
}