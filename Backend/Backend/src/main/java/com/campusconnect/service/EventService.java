package com.campusconnect.service;

import com.campusconnect.dto.*;
import com.campusconnect.entity.*;
import com.campusconnect.enums.EventCategory;
import com.campusconnect.enums.EventType;
import com.campusconnect.enums.NotificationType;
import com.campusconnect.enums.RsvpStatus;
import com.campusconnect.exception.BadRequestException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.exception.UnauthorizedException;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventRsvpRepository rsvpRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired(required = false)
    private AchievementService achievementService;

    @Transactional
    public EventDTO createEvent(Long userId, CreateEventRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        EventType type = parseEventType(req.getEventType());
        EventCategory category = parseEventCategory(req.getCategory());

        if (type == EventType.VIRTUAL && (req.getVirtualLink() == null || req.getVirtualLink().isBlank())) {
            throw new BadRequestException("Virtual events need a meeting link");
        }
        if (type == EventType.PHYSICAL && (req.getLocation() == null || req.getLocation().isBlank())) {
            throw new BadRequestException("Physical events need a location");
        }

        Event event = Event.builder()
                .createdBy(user)
                .title(req.getTitle())
                .description(req.getDescription())
                .eventDate(req.getEventDate())
                .eventEndDate(req.getEventEndDate())
                .location(req.getLocation())
                .imageUrl(req.getImageUrl())
                .coverUrl(req.getCoverUrl())
                .eventType(type)
                .category(category)
                .virtualLink(req.getVirtualLink())
                .maxParticipants(req.getMaxParticipants())
                .showAttendees(req.getShowAttendees() == null ? true : req.getShowAttendees())
                .isActive(true)
                .reminderSent(false)
                .build();

        Event saved = eventRepository.save(event);

        try {
            if (achievementService != null) achievementService.award(userId, "FIRST_EVENT_CREATED");
        } catch (Exception ignored) {}

        return mapToDTO(saved, userId);
    }

    @Transactional
    public EventDTO updateEvent(Long eventId, Long userId, CreateEventRequest req) {
        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (!e.getCreatedBy().getId().equals(userId))
            throw new UnauthorizedException("Only the organizer can edit this event");

        if (req.getTitle() != null) e.setTitle(req.getTitle());
        if (req.getDescription() != null) e.setDescription(req.getDescription());
        if (req.getEventDate() != null) e.setEventDate(req.getEventDate());
        if (req.getEventEndDate() != null) e.setEventEndDate(req.getEventEndDate());
        if (req.getLocation() != null) e.setLocation(req.getLocation());
        if (req.getImageUrl() != null) e.setImageUrl(req.getImageUrl());
        if (req.getCoverUrl() != null) e.setCoverUrl(req.getCoverUrl());
        if (req.getVirtualLink() != null) e.setVirtualLink(req.getVirtualLink());
        if (req.getMaxParticipants() != null) e.setMaxParticipants(req.getMaxParticipants());
        if (req.getShowAttendees() != null) e.setShowAttendees(req.getShowAttendees());
        if (req.getEventType() != null) e.setEventType(parseEventType(req.getEventType()));
        if (req.getCategory() != null) e.setCategory(parseEventCategory(req.getCategory()));

        eventRepository.save(e);
        return mapToDTO(e, userId);
    }

    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (!e.getCreatedBy().getId().equals(userId))
            throw new UnauthorizedException("Only the organizer can delete this event");
        e.setActive(false);
        eventRepository.save(e);
    }

    public Page<EventDTO> getUpcomingEvents(Long userId, int page, int size) {
        return eventRepository.findUpcoming(LocalDateTime.now(), PageRequest.of(page, size))
                .map(e -> mapToDTO(e, userId));
    }

    public Page<EventDTO> getPastEvents(Long userId, int page, int size) {
        return eventRepository.findPast(LocalDateTime.now(), PageRequest.of(page, size))
                .map(e -> mapToDTO(e, userId));
    }

    public Page<EventDTO> getThisWeekEvents(Long userId, int page, int size) {
        LocalDate today = LocalDate.now();
        WeekFields wf = WeekFields.of(Locale.getDefault());
        LocalDate startOfWeek = today.with(wf.dayOfWeek(), 1);
        LocalDate endOfWeek = startOfWeek.plusDays(7);
        return eventRepository.findThisWeek(
                        startOfWeek.atStartOfDay(), endOfWeek.atStartOfDay(), PageRequest.of(page, size))
                .map(e -> mapToDTO(e, userId));
    }

    public Page<EventDTO> getEventsByCategory(String category, Long userId, int page, int size) {
        EventCategory cat = parseEventCategory(category);
        return eventRepository.findUpcomingByCategory(cat, LocalDateTime.now(), PageRequest.of(page, size))
                .map(e -> mapToDTO(e, userId));
    }

    public Page<EventDTO> getMyEvents(Long userId, int page, int size) {
        return eventRepository.findByCreator(userId, PageRequest.of(page, size))
                .map(e -> mapToDTO(e, userId));
    }

    public EventDTO getEventById(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return mapToDTO(event, userId);
    }

    @Transactional
    public EventDTO setRsvp(Long eventId, Long userId, String statusStr) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        RsvpStatus status;
        try { status = RsvpStatus.valueOf(statusStr.toUpperCase()); }
        catch (Exception ex) { throw new BadRequestException("Invalid RSVP status"); }

        if (status == RsvpStatus.GOING && event.getMaxParticipants() != null) {
            long going = rsvpRepository.countByEventIdAndStatus(eventId, RsvpStatus.GOING);
            EventRsvp existing = rsvpRepository.findByEventIdAndUserId(eventId, userId).orElse(null);
            boolean alreadyGoing = existing != null && existing.getStatus() == RsvpStatus.GOING;
            if (!alreadyGoing && going >= event.getMaxParticipants()) {
                throw new BadRequestException("Event is full");
            }
        }

        EventRsvp rsvp = rsvpRepository.findByEventIdAndUserId(eventId, userId)
                .orElseGet(() -> EventRsvp.builder().event(event).user(user).status(status).build());
        rsvp.setStatus(status);
        rsvpRepository.save(rsvp);

        if (status == RsvpStatus.GOING && !event.getCreatedBy().getId().equals(userId)) {
            try {
                notificationService.createNotification(
                        event.getCreatedBy().getId(), userId, NotificationType.LIKE, eventId);
            } catch (Exception ignored) {}
        }

        return mapToDTO(event, userId);
    }

    @Transactional
    public void removeRsvp(Long eventId, Long userId) {
        rsvpRepository.findByEventIdAndUserId(eventId, userId).ifPresent(rsvpRepository::delete);
    }

    public List<EventAttendeeDTO> getAttendees(Long eventId, Long requesterId, String statusStr) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        boolean isOrganizer = event.getCreatedBy().getId().equals(requesterId);
        if (!event.isShowAttendees() && !isOrganizer) {
            throw new UnauthorizedException("Attendee list is hidden");
        }
        RsvpStatus status;
        try { status = RsvpStatus.valueOf(statusStr.toUpperCase()); }
        catch (Exception ex) { throw new BadRequestException("Invalid RSVP status"); }

        return rsvpRepository.findByEventAndStatus(eventId, status).stream().map(r -> {
            User u = r.getUser();
            return EventAttendeeDTO.builder()
                    .userId(u.getId()).name(u.getName())
                    .profilePicUrl(u.getProfilePicUrl())
                    .position(u.getPosition()).department(u.getDepartment())
                    .role(u.getRole().name()).status(r.getStatus().name())
                    .rsvpAt(r.getUpdatedAt()).build();
        }).collect(Collectors.toList());
    }

    public String generateIcs(Long eventId) {
        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        LocalDateTime start = e.getEventDate();
        LocalDateTime end = e.getEventEndDate() != null ? e.getEventEndDate() : start.plusHours(2);

        String dtStart = start.atOffset(ZoneOffset.UTC).format(fmt);
        String dtEnd = end.atOffset(ZoneOffset.UTC).format(fmt);
        String now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(fmt);
        String uid = "event-" + e.getId() + "@campus-connect";

        String summary = escapeIcs(e.getTitle());
        String desc = escapeIcs(e.getDescription() == null ? "" : e.getDescription());
        String location;
        if (e.getEventType() == EventType.VIRTUAL) {
            location = e.getVirtualLink() == null ? "Online" : e.getVirtualLink();
        } else if (e.getEventType() == EventType.HYBRID) {
            location = (e.getLocation() == null ? "" : e.getLocation())
                    + (e.getVirtualLink() == null ? "" : " | " + e.getVirtualLink());
        } else {
            location = e.getLocation() == null ? "" : e.getLocation();
        }
        location = escapeIcs(location);

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//Campus Connect//Events//EN\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(now).append("\r\n");
        sb.append("DTSTART:").append(dtStart).append("\r\n");
        sb.append("DTEND:").append(dtEnd).append("\r\n");
        sb.append("SUMMARY:").append(summary).append("\r\n");
        if (!desc.isBlank()) sb.append("DESCRIPTION:").append(desc).append("\r\n");
        if (!location.isBlank()) sb.append("LOCATION:").append(location).append("\r\n");
        sb.append("STATUS:CONFIRMED\r\n");
        sb.append("BEGIN:VALARM\r\n");
        sb.append("ACTION:DISPLAY\r\n");
        sb.append("DESCRIPTION:Reminder\r\n");
        sb.append("TRIGGER:-PT1H\r\n");
        sb.append("END:VALARM\r\n");
        sb.append("END:VEVENT\r\n");
        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String escapeIcs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(";", "\\;")
                .replace(",", "\\,").replace("\n", "\\n").replace("\r", "");
    }

    @Scheduled(fixedDelay = 600000)
    @Transactional
    public void sendEventReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime in1h = now.plusHours(1);
            LocalDateTime in75m = now.plusHours(1).plusMinutes(15);
            List<Event> events = eventRepository.findDueForReminder(in1h, in75m);
            for (Event e : events) {
                List<EventRsvp> attendees = rsvpRepository.findByEventAndStatus(e.getId(), RsvpStatus.GOING);
                for (EventRsvp r : attendees) {
                    try {
                        notificationService.createNotification(
                                r.getUser().getId(), e.getCreatedBy().getId(),
                                NotificationType.LIKE, e.getId());
                    } catch (Exception ignored) {}
                }
                e.setReminderSent(true);
                eventRepository.save(e);
            }
        } catch (Exception ignored) {}
    }

    public EventDTO mapToDTO(Event e, Long currentUserId) {
        long going = rsvpRepository.countByEventIdAndStatus(e.getId(), RsvpStatus.GOING);
        long interested = rsvpRepository.countByEventIdAndStatus(e.getId(), RsvpStatus.INTERESTED);
        String myStatus = null;
        if (currentUserId != null) {
            EventRsvp mine = rsvpRepository.findByEventIdAndUserId(e.getId(), currentUserId).orElse(null);
            if (mine != null) myStatus = mine.getStatus().name();
        }
        LocalDateTime now = LocalDateTime.now();
        boolean isPast = e.getEventEndDate() != null
                ? e.getEventEndDate().isBefore(now) : e.getEventDate().plusHours(3).isBefore(now);
        LocalDateTime liveFrom = e.getEventDate().minusHours(1);
        LocalDateTime liveUntil = e.getEventEndDate() != null ? e.getEventEndDate() : e.getEventDate().plusHours(3);
        boolean isLive = now.isAfter(liveFrom) && now.isBefore(liveUntil);

        return EventDTO.builder()
                .id(e.getId()).title(e.getTitle()).description(e.getDescription())
                .eventDate(e.getEventDate()).eventEndDate(e.getEventEndDate())
                .location(e.getLocation()).imageUrl(e.getImageUrl()).coverUrl(e.getCoverUrl())
                .eventType(e.getEventType() == null ? "PHYSICAL" : e.getEventType().name())
                .category(e.getCategory() == null ? "OTHER" : e.getCategory().name())
                .virtualLink(e.getVirtualLink()).maxParticipants(e.getMaxParticipants())
                .showAttendees(e.isShowAttendees())
                .createdById(e.getCreatedBy().getId())
                .createdByName(e.getCreatedBy().getName())
                .createdByProfilePic(e.getCreatedBy().getProfilePicUrl())
                .createdByRole(e.getCreatedBy().getRole().name())
                .createdAt(e.getCreatedAt())
                .goingCount(going).interestedCount(interested)
                .myRsvpStatus(myStatus)
                .isPast(isPast).isLive(isLive)
                .build();
    }

    private EventType parseEventType(String s) {
        if (s == null || s.isBlank()) return EventType.PHYSICAL;
        try { return EventType.valueOf(s.toUpperCase()); }
        catch (Exception ex) { return EventType.PHYSICAL; }
    }

    private EventCategory parseEventCategory(String s) {
        if (s == null || s.isBlank()) return EventCategory.OTHER;
        try { return EventCategory.valueOf(s.toUpperCase()); }
        catch (Exception ex) { return EventCategory.OTHER; }
    }
}