package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.ChatRoomDTO;
import com.campusconnect.dto.CreateRoomRequest;
import com.campusconnect.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService roomService;

    @GetMapping
    public ResponseEntity<List<ChatRoomDTO>> myRooms(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(roomService.getMyRooms(user.getId()));
    }

    @PostMapping
    public ResponseEntity<ChatRoomDTO> createRoom(@AuthenticationPrincipal CustomUserDetails user,
                                                  @Valid @RequestBody CreateRoomRequest req) {
        return ResponseEntity.ok(roomService.createRoom(user.getId(), req));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDTO> getRoom(@AuthenticationPrincipal CustomUserDetails user,
                                               @PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoom(roomId, user.getId()));
    }

    @PostMapping("/{roomId}/members")
    public ResponseEntity<ChatRoomDTO> addMembers(@AuthenticationPrincipal CustomUserDetails user,
                                                  @PathVariable Long roomId,
                                                  @RequestBody Map<String, List<Long>> payload) {
        return ResponseEntity.ok(roomService.addMembers(roomId, user.getId(), payload.get("userIds")));
    }

    @DeleteMapping("/{roomId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@AuthenticationPrincipal CustomUserDetails user,
                                             @PathVariable Long roomId,
                                             @PathVariable Long userId) {
        roomService.removeMember(roomId, user.getId(), userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(@AuthenticationPrincipal CustomUserDetails user,
                                          @PathVariable Long roomId) {
        roomService.removeMember(roomId, user.getId(), user.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roomId}/rename")
    public ResponseEntity<Void> renameRoom(@AuthenticationPrincipal CustomUserDetails user,
                                           @PathVariable Long roomId,
                                           @RequestBody Map<String, String> payload) {
        roomService.renameRoom(roomId, user.getId(), payload.get("name"));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roomId}/read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal CustomUserDetails user,
                                         @PathVariable Long roomId) {
        roomService.markRoomRead(roomId, user.getId());
        return ResponseEntity.noContent().build();
    }
}