package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.NotificationDTO;
import com.campusconnect.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getNotifications(user.getId(), page, size));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(user.getId())));
    }

    @PutMapping("/read")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal CustomUserDetails user) {
        notificationService.markAllRead(user.getId());
        return ResponseEntity.ok().build();
    }
}