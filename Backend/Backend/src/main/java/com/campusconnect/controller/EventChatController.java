package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.EventChatMessageDTO;
import com.campusconnect.dto.SendEventChatRequest;
import com.campusconnect.service.EventChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/events/{eventId}/chat")
@RequiredArgsConstructor
public class EventChatController {

    private final EventChatService chatService;

    @GetMapping("/access")
    public ResponseEntity<Map<String, Boolean>> access(@PathVariable Long eventId,
                                                       @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(Map.of("canAccess", chatService.canAccessChat(eventId, user.getId())));
    }

    @GetMapping
    public ResponseEntity<Page<EventChatMessageDTO>> getMessages(@PathVariable Long eventId,
                                                                 @AuthenticationPrincipal CustomUserDetails user,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(chatService.getMessages(eventId, user.getId(), page, size));
    }

    @PostMapping
    public ResponseEntity<EventChatMessageDTO> send(@PathVariable Long eventId,
                                                    @AuthenticationPrincipal CustomUserDetails user,
                                                    @Valid @RequestBody SendEventChatRequest req) {
        return ResponseEntity.ok(chatService.sendMessage(eventId, user.getId(), req));
    }
}