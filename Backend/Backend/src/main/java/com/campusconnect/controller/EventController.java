package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.*;
import com.campusconnect.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@AuthenticationPrincipal CustomUserDetails user,
                                                @Valid @RequestBody CreateEventRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(user.getId(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id,
                                                @AuthenticationPrincipal CustomUserDetails user,
                                                @RequestBody CreateEventRequest req) {
        return ResponseEntity.ok(eventService.updateEvent(id, user.getId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id,
                                            @AuthenticationPrincipal CustomUserDetails user) {
        eventService.deleteEvent(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<EventDTO>> getEvents(@AuthenticationPrincipal CustomUserDetails user,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.getUpcomingEvents(user.getId(), page, size));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventDTO>> getUpcoming(@AuthenticationPrincipal CustomUserDetails user,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.getUpcomingEvents(user.getId(), page, size));
    }

    @GetMapping("/past")
    public ResponseEntity<Page<EventDTO>> getPast(@AuthenticationPrincipal CustomUserDetails user,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.getPastEvents(user.getId(), page, size));
    }

    @GetMapping("/this-week")
    public ResponseEntity<Page<EventDTO>> getThisWeek(@AuthenticationPrincipal CustomUserDetails user,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.getThisWeekEvents(user.getId(), page, size));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<EventDTO>> getByCategory(@PathVariable String category,
                                                        @AuthenticationPrincipal CustomUserDetails user,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.getEventsByCategory(category, user.getId(), page, size));
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<EventDTO>> mine(@AuthenticationPrincipal CustomUserDetails user,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.getMyEvents(user.getId(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(eventService.getEventById(id, user.getId()));
    }

    @PostMapping("/{id}/rsvp")
    public ResponseEntity<EventDTO> setRsvp(@PathVariable Long id,
                                            @AuthenticationPrincipal CustomUserDetails user,
                                            @Valid @RequestBody RsvpRequest req) {
        return ResponseEntity.ok(eventService.setRsvp(id, user.getId(), req.getStatus()));
    }

    @DeleteMapping("/{id}/rsvp")
    public ResponseEntity<Void> removeRsvp(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetails user) {
        eventService.removeRsvp(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/attendees")
    public ResponseEntity<List<EventAttendeeDTO>> attendees(@PathVariable Long id,
                                                            @AuthenticationPrincipal CustomUserDetails user,
                                                            @RequestParam(defaultValue = "GOING") String status) {
        return ResponseEntity.ok(eventService.getAttendees(id, user.getId(), status));
    }

    @GetMapping("/{id}/ics")
    public ResponseEntity<Resource> downloadIcs(@PathVariable Long id) {
        String ics = eventService.generateIcs(id);
        byte[] bytes = ics.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"event-" + id + ".ics\"")
                .contentLength(bytes.length)
                .body(resource);
    }
}