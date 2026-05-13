package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.*;
import com.campusconnect.service.ConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/request")
    public ResponseEntity<ConnectionDTO> sendRequest(@AuthenticationPrincipal CustomUserDetails user,
                                                     @Valid @RequestBody ConnectionRequestBody body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(connectionService.sendRequest(user.getId(), body.getReceiverId(), body.getMessage()));
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

    @DeleteMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomUserDetails user,
                                         @PathVariable Long id) {
        connectionService.withdrawRequest(user.getId(), id);
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

    @GetMapping("/sent")
    public ResponseEntity<Page<ConnectionDTO>> getSent(@AuthenticationPrincipal CustomUserDetails user,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(connectionService.getSentRequests(user.getId(), page, size));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<ConnectionSuggestionDTO>> getSuggestions(@AuthenticationPrincipal CustomUserDetails user,
                                                                        @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(connectionService.getSuggestions(user.getId(), Math.min(limit, 50)));
    }

    @GetMapping("/mutuals/{otherUserId}")
    public ResponseEntity<List<MutualConnectionDTO>> getMutuals(@AuthenticationPrincipal CustomUserDetails user,
                                                                @PathVariable Long otherUserId,
                                                                @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(connectionService.getMutualConnections(user.getId(), otherUserId, Math.min(limit, 50)));
    }

    @GetMapping("/mutuals/{otherUserId}/count")
    public ResponseEntity<Integer> getMutualCount(@AuthenticationPrincipal CustomUserDetails user,
                                                  @PathVariable Long otherUserId) {
        return ResponseEntity.ok(connectionService.getMutualConnectionCount(user.getId(), otherUserId));
    }
}