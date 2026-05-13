package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.*;
import com.campusconnect.service.SkillEndorsementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/endorsements")
@RequiredArgsConstructor
public class EndorsementController {

    private final SkillEndorsementService service;

    @PostMapping
    public ResponseEntity<SkillEndorsementDTO> endorse(@AuthenticationPrincipal CustomUserDetails user,
                                                       @Valid @RequestBody CreateEndorsementRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.endorse(user.getId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal CustomUserDetails user,
                                       @PathVariable Long id) {
        service.removeEndorsement(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<SkillEndorsementDTO>> getReceived(@PathVariable Long userId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getEndorsementsReceivedBy(userId, page, size));
    }

    @GetMapping("/user/{userId}/by/{endorserId}")
    public ResponseEntity<List<SkillEndorsementDTO>> getEndorsementsByEndorser(@PathVariable Long userId,
                                                                               @PathVariable Long endorserId) {
        return ResponseEntity.ok(service.getEndorsementsByEndorser(userId, endorserId));
    }

    @GetMapping("/user/{userId}/skill/{skill}")
    public ResponseEntity<List<SkillEndorsementDTO>> getEndorsersForSkill(@PathVariable Long userId,
                                                                          @PathVariable String skill) {
        return ResponseEntity.ok(service.getEndorsersForSkill(userId, skill));
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<EndorsementSummaryDTO> getSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getSummary(userId));
    }
}