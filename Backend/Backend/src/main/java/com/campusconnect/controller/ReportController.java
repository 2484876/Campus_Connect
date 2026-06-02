package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.ReportDTO;
import com.campusconnect.dto.ReportRequest;
import com.campusconnect.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/reports")
    public ResponseEntity<ReportDTO> submit(@AuthenticationPrincipal CustomUserDetails user,
                                            @Valid @RequestBody ReportRequest req) {
        return ResponseEntity.ok(reportService.submitReport(user.getId(), req));
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<Map<String, String>> block(@PathVariable Long id,
                                                     @AuthenticationPrincipal CustomUserDetails user) {
        reportService.blockUser(user.getId(), id);
        return ResponseEntity.ok(Map.of("status", "blocked"));
    }

    @DeleteMapping("/users/{id}/block")
    public ResponseEntity<Map<String, String>> unblock(@PathVariable Long id,
                                                       @AuthenticationPrincipal CustomUserDetails user) {
        reportService.unblockUser(user.getId(), id);
        return ResponseEntity.ok(Map.of("status", "unblocked"));
    }

    @GetMapping("/users/{id}/block-status")
    public ResponseEntity<Map<String, Boolean>> status(@PathVariable Long id,
                                                       @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(Map.of("blocked", reportService.isBlocked(user.getId(), id)));
    }
}