package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.*;
import com.campusconnect.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@AuthenticationPrincipal CustomUserDetails user,
                                                @Valid @RequestBody CreateEventRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(user.getId(), req));
    }

    @GetMapping
    public ResponseEntity<Page<EventDTO>> getEvents(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.getAllEvents(page, size));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventDTO>> getUpcoming(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.getUpcomingEvents(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }
}