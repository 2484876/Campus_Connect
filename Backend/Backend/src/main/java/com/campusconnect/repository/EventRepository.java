package com.campusconnect.repository;
import com.campusconnect.entity.Event;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.eventDate >= :now ORDER BY e.eventDate ASC")
    Page<Event> findUpcoming(LocalDateTime now, Pageable pageable);
    @Query("SELECT e FROM Event e WHERE e.isActive = true ORDER BY e.createdAt DESC")
    Page<Event> findAllActive(Pageable pageable);
}