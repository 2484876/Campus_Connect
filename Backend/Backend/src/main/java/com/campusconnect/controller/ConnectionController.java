package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.ConnectionDTO;
import com.campusconnect.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/request")
    public ResponseEntity<ConnectionDTO> sendRequest(@AuthenticationPrincipal CustomUserDetails user,
                                                     @RequestBody Map<String, Long> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(connectionService.sendRequest(user.getId(), body.get("receiverId")));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ConnectionDTO> accept(@AuthenticationPrincipal CustomUserDetails user,
                                                @PathVariable Long id) {
        return ResponseEntity.ok(connectionService.acceptRequest(user.getId(), id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@AuthenticationPrincipal CustomUserDetails user,
                                       @PathVariable Long id) {
        connectionService.rejectRequest(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal CustomUserDetails user,
                                       @PathVariable Long id) {
        connectionService.removeConnection(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ConnectionDTO>> getConnections(@AuthenticationPrincipal CustomUserDetails user,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(connectionService.getConnections(user.getId(), page, size));
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<ConnectionDTO>> getPending(@AuthenticationPrincipal CustomUserDetails user,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(connectionService.getPendingRequests(user.getId(), page, size));
    }
}