package com.campusconnect.service;

import com.campusconnect.dto.*;
import com.campusconnect.entity.*;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public EventDTO createEvent(Long userId, CreateEventRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Event event = Event.builder()
                .createdBy(user)
                .title(req.getTitle())
                .description(req.getDescription())
                .eventDate(req.getEventDate())
                .location(req.getLocation())
                .imageUrl(req.getImageUrl())
                .maxParticipants(req.getMaxParticipants())
                .isActive(true)
                .build();
        Event saved = eventRepository.save(event);
        return mapToDTO(saved);
    }

    public Page<EventDTO> getUpcomingEvents(int page, int size) {
        return eventRepository.findUpcoming(LocalDateTime.now(), PageRequest.of(page, size))
                .map(this::mapToDTO);
    }

    public Page<EventDTO> getAllEvents(int page, int size) {
        return eventRepository.findAllActive(PageRequest.of(page, size))
                .map(this::mapToDTO);
    }

    public EventDTO getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return mapToDTO(event);
    }

    private EventDTO mapToDTO(Event e) {
        return EventDTO.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .eventDate(e.getEventDate())
                .location(e.getLocation())
                .imageUrl(e.getImageUrl())
                .maxParticipants(e.getMaxParticipants())
                .createdById(e.getCreatedBy().getId())
                .createdByName(e.getCreatedBy().getName())
                .createdAt(e.getCreatedAt())
                .build();
    }
}