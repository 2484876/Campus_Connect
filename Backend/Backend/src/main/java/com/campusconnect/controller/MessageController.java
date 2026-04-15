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
            Long receiverId = Long.valueOf(payload.get("receiverId").toString());
            boolean isTyping = (boolean) payload.get("typing");
            chatService.broadcastTyping(senderId, receiverId, isTyping);
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

    @PostMapping("/api/messages/typing")
    public ResponseEntity<Void> typing(@AuthenticationPrincipal CustomUserDetails user,
                                       @RequestBody Map<String, Object> payload) {
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        boolean isTyping = (boolean) payload.get("typing");
        chatService.broadcastTyping(user.getId(), receiverId, isTyping);
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