package com.campusconnect.repository;

import com.campusconnect.entity.EventChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventChatMessageRepository extends JpaRepository<EventChatMessage, Long> {
    Page<EventChatMessage> findByEventIdOrderByCreatedAtAsc(Long eventId, Pageable pageable);
}