package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.PresenceDTO;
import com.campusconnect.enums.PresenceStatus;
import com.campusconnect.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(@AuthenticationPrincipal CustomUserDetails user) {
        presenceService.setStatus(user.getId(), PresenceStatus.ONLINE);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/status")
    public ResponseEntity<Void> setStatus(@AuthenticationPrincipal CustomUserDetails user,
                                          @RequestBody Map<String, String> payload) {
        PresenceStatus status = PresenceStatus.OFFLINE;
        try {
            status = PresenceStatus.valueOf(payload.get("status"));
        } catch (Exception ignored) {}
        presenceService.setStatus(user.getId(), status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PresenceDTO> getOne(@PathVariable Long userId) {
        return ResponseEntity.ok(presenceService.getPresence(userId));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<PresenceDTO>> bulk(@RequestBody Map<String, List<Long>> payload) {
        return ResponseEntity.ok(presenceService.bulk(payload.get("userIds")));
    }
}