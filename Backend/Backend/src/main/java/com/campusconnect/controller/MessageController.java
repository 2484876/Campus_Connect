package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.*;
import com.campusconnect.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendViaWebSocket(@Payload SendMessageRequest req,
                                 SimpMessageHeaderAccessor headerAccessor) {
        Long senderId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (senderId != null) {
            chatService.sendMessage(senderId, req);
        }
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload Map<String, Object> payload,
                       SimpMessageHeaderAccessor headerAccessor) {
        Long senderId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (senderId != null) {
            Long receiverId = payload.get("receiverId") != null
                    ? Long.valueOf(payload.get("receiverId").toString()) : null;
            Long roomId = payload.get("roomId") != null
                    ? Long.valueOf(payload.get("roomId").toString()) : null;
            boolean isTyping = (boolean) payload.get("typing");
            chatService.broadcastTyping(senderId, receiverId, roomId, isTyping);
        }
    }

    @MessageMapping("/chat.react")
    public void reactViaWebSocket(@Payload ReactionRequest req,
                                  SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            chatService.toggleReaction(userId, req);
        }
    }

    @PostMapping("/api/messages")
    public ResponseEntity<MessageDTO> sendMessage(@AuthenticationPrincipal CustomUserDetails user,
                                                  @Valid @RequestBody SendMessageRequest req) {
        return ResponseEntity.ok(chatService.sendMessage(user.getId(), req));
    }

    @GetMapping("/api/messages/{otherUserId}")
    public ResponseEntity<Page<MessageDTO>> getConversation(@AuthenticationPrincipal CustomUserDetails user,
                                                            @PathVariable Long otherUserId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(chatService.getConversation(user.getId(), otherUserId, page, size));
    }

    @GetMapping("/api/rooms/{roomId}/messages")
    public ResponseEntity<Page<MessageDTO>> getRoomMessages(@AuthenticationPrincipal CustomUserDetails user,
                                                            @PathVariable Long roomId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(chatService.getRoomMessages(user.getId(), roomId, page, size));
    }

    @GetMapping("/api/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(chatService.getConversations(user.getId()));
    }

    @PutMapping("/api/messages/read/{senderId}")
    public ResponseEntity<ReadReceiptDTO> markAsRead(@AuthenticationPrincipal CustomUserDetails user,
                                                     @PathVariable Long senderId) {
        return ResponseEntity.ok(chatService.markAsRead(user.getId(), senderId));
    }

    @PostMapping("/api/messages/delete")
    public ResponseEntity<MessageDeleteDTO> deleteMessage(@AuthenticationPrincipal CustomUserDetails user,
                                                          @Valid @RequestBody DeleteMessageRequest req) {
        return ResponseEntity.ok(chatService.deleteMessage(user.getId(), req));
    }

    @PutMapping("/api/messages/{messageId}/edit")
    public ResponseEntity<MessageDTO> editMessage(@AuthenticationPrincipal CustomUserDetails user,
                                                  @PathVariable Long messageId,
                                                  @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(chatService.editMessage(user.getId(), messageId, payload.get("content")));
    }

    @PostMapping("/api/messages/{messageId}/pin")
    public ResponseEntity<MessageDTO> pinMessage(@AuthenticationPrincipal CustomUserDetails user,
                                                 @PathVariable Long messageId) {
        return ResponseEntity.ok(chatService.pinMessage(user.getId(), messageId));
    }

    @DeleteMapping("/api/messages/{messageId}/pin")
    public ResponseEntity<Void> unpinMessage(@AuthenticationPrincipal CustomUserDetails user,
                                             @PathVariable Long messageId) {
        chatService.unpinMessage(user.getId(), messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/pinned/dm/{otherUserId}")
    public ResponseEntity<List<MessageDTO>> getPinnedDm(@AuthenticationPrincipal CustomUserDetails user,
                                                        @PathVariable Long otherUserId) {
        return ResponseEntity.ok(chatService.getPinned(user.getId(), otherUserId, null));
    }

    @GetMapping("/api/pinned/room/{roomId}")
    public ResponseEntity<List<MessageDTO>> getPinnedRoom(@AuthenticationPrincipal CustomUserDetails user,
                                                          @PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getPinned(user.getId(), null, roomId));
    }

    @GetMapping("/api/messages/search")
    public ResponseEntity<List<MessageDTO>> search(@AuthenticationPrincipal CustomUserDetails user,
                                                   @RequestParam String q) {
        return ResponseEntity.ok(chatService.searchMessages(user.getId(), q));
    }

    @PostMapping("/api/messages/typing")
    public ResponseEntity<Void> typing(@AuthenticationPrincipal CustomUserDetails user,
                                       @RequestBody Map<String, Object> payload) {
        Long receiverId = payload.get("receiverId") != null
                ? Long.valueOf(payload.get("receiverId").toString()) : null;
        Long roomId = payload.get("roomId") != null
                ? Long.valueOf(payload.get("roomId").toString()) : null;
        boolean isTyping = (boolean) payload.get("typing");
        chatService.broadcastTyping(user.getId(), receiverId, roomId, isTyping);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/messages/react")
    public ResponseEntity<ReactionDTO> toggleReaction(@AuthenticationPrincipal CustomUserDetails user,
                                                      @Valid @RequestBody ReactionRequest req) {
        ReactionDTO result = chatService.toggleReaction(user.getId(), req);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/messages/{messageId}/reactions")
    public ResponseEntity<List<ReactionDTO>> getReactions(@AuthenticationPrincipal CustomUserDetails user,
                                                          @PathVariable Long messageId) {
        return ResponseEntity.ok(chatService.getReactions(user.getId(), messageId));
    }
}