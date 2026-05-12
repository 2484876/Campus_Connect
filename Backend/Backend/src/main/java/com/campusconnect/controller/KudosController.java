package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.CreateKudosRequest;
import com.campusconnect.dto.KudosDTO;
import com.campusconnect.service.KudosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/kudos")
@RequiredArgsConstructor
public class KudosController {

    private final KudosService kudosService;

    @PostMapping
    public ResponseEntity<KudosDTO> give(@AuthenticationPrincipal CustomUserDetails user,
                                         @Valid @RequestBody CreateKudosRequest req) {
        return ResponseEntity.ok(kudosService.give(user.getId(), req));
    }

    @GetMapping("/received/{userId}")
    public ResponseEntity<Page<KudosDTO>> received(@PathVariable Long userId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(kudosService.getReceived(userId, page, size));
    }

    @GetMapping("/given/{userId}")
    public ResponseEntity<Page<KudosDTO>> given(@PathVariable Long userId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(kudosService.getGiven(userId, page, size));
    }

    @GetMapping("/recent")
    public ResponseEntity<Page<KudosDTO>> recent(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(kudosService.getRecent(page, size));
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Long>> stats(@PathVariable Long userId) {
        return ResponseEntity.ok(kudosService.getStats(userId));
    }
}