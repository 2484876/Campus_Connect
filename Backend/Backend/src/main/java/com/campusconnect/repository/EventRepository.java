package com.campusconnect.repository;

import com.campusconnect.entity.Event;
import com.campusconnect.enums.EventCategory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.eventDate >= :now ORDER BY e.eventDate ASC")
    Page<Event> findUpcoming(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.eventDate < :now ORDER BY e.eventDate DESC")
    Page<Event> findPast(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.eventDate BETWEEN :start AND :end ORDER BY e.eventDate ASC")
    Page<Event> findThisWeek(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.category = :category AND e.eventDate >= :now ORDER BY e.eventDate ASC")
    Page<Event> findUpcomingByCategory(@Param("category") EventCategory category, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.isActive = true ORDER BY e.createdAt DESC")
    Page<Event> findAllActive(Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.reminderSent = false AND e.eventDate BETWEEN :from AND :to")
    List<Event> findDueForReminder(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.createdBy.id = :userId ORDER BY e.eventDate DESC")
    Page<Event> findByCreator(@Param("userId") Long userId, Pageable pageable);
}