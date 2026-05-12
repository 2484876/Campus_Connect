package com.campusconnect.service;

import com.campusconnect.dto.EventChatMessageDTO;
import com.campusconnect.dto.SendEventChatRequest;
import com.campusconnect.entity.Event;
import com.campusconnect.entity.EventChatMessage;
import com.campusconnect.entity.User;
import com.campusconnect.enums.RsvpStatus;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.exception.UnauthorizedException;
import com.campusconnect.repository.EventChatMessageRepository;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.EventRsvpRepository;
import com.campusconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventChatService {

    private final EventChatMessageRepository chatRepository;
    private final EventRepository eventRepository;
    private final EventRsvpRepository rsvpRepository;
    private final UserRepository userRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    public boolean canAccessChat(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null || !event.isActive()) return false;
        if (event.getCreatedBy().getId().equals(userId)) return true;
        return rsvpRepository.existsByEventIdAndUserIdAndStatus(eventId, userId, RsvpStatus.GOING);
    }

    private void assertAccess(Long eventId, Long userId) {
        if (!canAccessChat(eventId, userId)) {
            throw new UnauthorizedException("Only attendees marked GOING can access event chat");
        }
    }

    public Page<EventChatMessageDTO> getMessages(Long eventId, Long userId, int page, int size) {
        assertAccess(eventId, userId);
        return chatRepository.findByEventIdOrderByCreatedAtAsc(eventId, PageRequest.of(page, size))
                .map(this::toDTO);
    }

    @Transactional
    public EventChatMessageDTO sendMessage(Long eventId, Long userId, SendEventChatRequest req) {
        assertAccess(eventId, userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        EventChatMessage msg = EventChatMessage.builder()
                .event(event).user(user).content(req.getContent()).build();
        EventChatMessage saved = chatRepository.save(msg);
        EventChatMessageDTO dto = toDTO(saved);

        try {
            if (messagingTemplate != null) {
                messagingTemplate.convertAndSend("/topic/event-chat/" + eventId, dto);
            }
        } catch (Exception ignored) {}

        return dto;
    }

    private EventChatMessageDTO toDTO(EventChatMessage m) {
        User u = m.getUser();
        return EventChatMessageDTO.builder()
                .id(m.getId()).eventId(m.getEvent().getId())
                .userId(u.getId()).userName(u.getName())
                .userProfilePic(u.getProfilePicUrl())
                .userRole(u.getRole().name())
                .content(m.getContent()).createdAt(m.getCreatedAt())
                .build();
    }
}