package com.campusconnect.service;

import com.campusconnect.dto.*;
import com.campusconnect.entity.*;
import com.campusconnect.enums.AttachmentType;
import com.campusconnect.enums.MessageType;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.exception.UnauthorizedException;
import com.campusconnect.repository.*;
import com.campusconnect.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final MessageReactionRepository reactionRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final PinnedMessageRepository pinnedRepository;
    private final ChatRoomRepository roomRepository;
    private final ChatRoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EncryptionUtil encryption;
    private final PresenceService presenceService;

    private static final String DELETED_MSG = "This message was deleted";
    private static final int MAX_PINNED_PER_CHAT = 3;

    @Transactional
    public MessageDTO sendMessage(Long senderId, SendMessageRequest req) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        boolean isRoom = req.getChatRoomId() != null;
        User receiver = null;
        ChatRoom room = null;

        if (isRoom) {
            room = roomRepository.findById(req.getChatRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
            if (!roomMemberRepository.existsByRoomIdAndUserId(room.getId(), senderId)) {
                throw new UnauthorizedException("Not a member of this room");
            }
        } else {
            if (req.getReceiverId() == null) {
                throw new IllegalArgumentException("receiverId or chatRoomId required");
            }
            receiver = userRepository.findById(req.getReceiverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));
        }

        String rawContent = req.getContent() == null ? "" : req.getContent();
        String encryptedContent = encryption.encrypt(rawContent);

        MessageType type = MessageType.TEXT;
        if (req.getMessageType() != null) {
            try { type = MessageType.valueOf(req.getMessageType()); } catch (Exception ignored) {}
        }

        Message.MessageBuilder builder = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .chatRoomId(isRoom ? room.getId() : null)
                .content(encryptedContent)
                .messageType(type)
                .readStatus(false)
                .deleted(false)
                .edited(false);

        if (req.getReplyToId() != null) {
            Message replyTo = messageRepository.findById(req.getReplyToId()).orElse(null);
            builder.replyTo(replyTo);
        }

        Message saved = messageRepository.save(builder.build());

        if (req.getAttachments() != null && !req.getAttachments().isEmpty()) {
            for (AttachmentDTO a : req.getAttachments()) {
                AttachmentType at = AttachmentType.FILE;
                if (a.getAttachmentType() != null) {
                    try { at = AttachmentType.valueOf(a.getAttachmentType()); } catch (Exception ignored) {}
                }
                MessageAttachment ma = MessageAttachment.builder()
                        .message(saved)
                        .attachmentType(at)
                        .url(a.getUrl())
                        .fileName(a.getFileName())
                        .fileSize(a.getFileSize())
                        .durationSeconds(a.getDurationSeconds())
                        .build();
                attachmentRepository.save(ma);
                saved.getAttachments().add(ma);
            }
        }

        if (isRoom) {
            room.setUpdatedAt(LocalDateTime.now());
            roomRepository.save(room);
        }

        MessageDTO dto = mapToDTO(saved);

        if (isRoom) {
            List<Long> memberIds = roomMemberRepository.findUserIdsByRoomId(room.getId());
            for (Long uid : memberIds) {
                messagingTemplate.convertAndSend("/queue/messages/" + uid, dto);
            }
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), dto);
        } else {
            messagingTemplate.convertAndSend("/queue/messages/" + receiver.getId(), dto);
            messagingTemplate.convertAndSend("/queue/messages/" + sender.getId(), dto);
        }

        return dto;
    }

    public Page<MessageDTO> getConversation(Long userId, Long otherUserId, int page, int size) {
        return messageRepository.findConversation(userId, otherUserId, PageRequest.of(page, size))
                .map(this::mapToDTO);
    }

    public Page<MessageDTO> getRoomMessages(Long userId, Long roomId, int page, int size) {
        if (!roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new UnauthorizedException("Not a member of this room");
        }
        return messageRepository.findRoomMessages(roomId, userId, PageRequest.of(page, size))
                .map(this::mapToDTO);
    }

    public List<ConversationDTO> getConversations(Long userId) {
        List<ConversationDTO> all = new ArrayList<>();

        List<Long> partnerIds = messageRepository.findConversationPartnerIds(userId);
        Map<Long, PresenceDTO> presenceMap = presenceService.bulk(partnerIds).stream()
                .collect(Collectors.toMap(PresenceDTO::getUserId, p -> p, (a, b) -> a));

        for (Long partnerId : partnerIds) {
            User partner = userRepository.findById(partnerId).orElse(null);
            if (partner == null) continue;

            Message lastMsg = messageRepository.findLastMessage(userId, partnerId);
            int unread = messageRepository.countByReceiverIdAndSenderIdAndReadStatusFalse(userId, partnerId);

            String preview = previewOf(lastMsg);
            String presence = presenceMap.containsKey(partnerId)
                    ? presenceMap.get(partnerId).getStatus() : "OFFLINE";

            all.add(ConversationDTO.builder()
                    .kind("DM")
                    .userId(partner.getId())
                    .userName(partner.getName())
                    .userProfilePic(partner.getProfilePicUrl())
                    .lastMessage(preview)
                    .lastMessageTime(lastMsg != null ? lastMsg.getCreatedAt() : null)
                    .unreadCount(unread)
                    .presence(presence)
                    .build());
        }

        List<ChatRoom> rooms = roomRepository.findRoomsForUser(userId);
        for (ChatRoom room : rooms) {
            Message last = messageRepository.findLastRoomMessage(room.getId());
            ChatRoomMember mySelf = roomMemberRepository.findByRoomIdAndUserId(room.getId(), userId).orElse(null);
            LocalDateTime lastRead = mySelf != null && mySelf.getLastReadAt() != null
                    ? mySelf.getLastReadAt() : LocalDateTime.now().minusYears(10);
            int unread = messageRepository.countRoomUnreadAfter(room.getId(), userId, lastRead);
            int memberCount = roomMemberRepository.findByRoomId(room.getId()).size();

            all.add(ConversationDTO.builder()
                    .kind("ROOM")
                    .roomId(room.getId())
                    .userName(room.getName())
                    .userProfilePic(room.getAvatarUrl())
                    .lastMessage(previewOf(last))
                    .lastMessageTime(last != null ? last.getCreatedAt() : room.getCreatedAt())
                    .unreadCount(unread)
                    .memberCount(memberCount)
                    .build());
        }

        all.sort((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });
        return all;
    }

    @Transactional
    public ReadReceiptDTO markAsRead(Long userId, Long senderId) {
        LocalDateTime now = LocalDateTime.now();
        List<Long> updatedIds = messageRepository.findUnreadMessageIds(userId, senderId);
        if (!updatedIds.isEmpty()) {
            messageRepository.markAsReadWithTimestamp(userId, senderId, now);
        }
        ReadReceiptDTO receipt = ReadReceiptDTO.builder()
                .readByUserId(userId)
                .senderUserId(senderId)
                .messageIds(updatedIds)
                .readAt(now)
                .build();
        messagingTemplate.convertAndSend("/queue/read-receipt/" + senderId, receipt);
        return receipt;
    }

    @Transactional
    public MessageDeleteDTO deleteMessage(Long userId, DeleteMessageRequest req) {
        Message message = messageRepository.findById(req.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        boolean isRoom = message.getChatRoomId() != null;
        Long otherUserId = null;
        if (!isRoom) {
            otherUserId = message.getSender().getId().equals(userId)
                    ? message.getReceiver().getId()
                    : message.getSender().getId();
        }

        String type = req.getDeleteType();

        if ("FOR_EVERYONE".equals(type)) {
            if (!message.getSender().getId().equals(userId)) {
                throw new RuntimeException("You can only delete your own sent messages for everyone");
            }
            if (message.getCreatedAt().plusHours(1).isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Cannot delete for everyone after 1 hour");
            }
            message.setDeleted(true);
            message.setDeletedBy(userId);
            message.setDeletedAt(LocalDateTime.now());
            message.setDeleteType("FOR_EVERYONE");
            message.setContent(encryption.encrypt(DELETED_MSG));
            reactionRepository.deleteByMessageId(req.getMessageId());
            messageRepository.save(message);

            MessageDeleteDTO dto = MessageDeleteDTO.builder()
                    .messageId(req.getMessageId())
                    .deletedBy(userId)
                    .otherUserId(otherUserId)
                    .deleteType("FOR_EVERYONE")
                    .build();

            if (isRoom) {
                messagingTemplate.convertAndSend("/topic/room/" + message.getChatRoomId(), dto);
                List<Long> memberIds = roomMemberRepository.findUserIdsByRoomId(message.getChatRoomId());
                for (Long uid : memberIds) {
                    messagingTemplate.convertAndSend("/queue/message-deleted/" + uid, dto);
                }
            } else {
                messagingTemplate.convertAndSend("/queue/message-deleted/" + otherUserId, dto);
                messagingTemplate.convertAndSend("/queue/message-deleted/" + userId, dto);
            }
            return dto;
        } else {
            message.setHiddenFor(userId);
            messageRepository.save(message);
            MessageDeleteDTO dto = MessageDeleteDTO.builder()
                    .messageId(req.getMessageId())
                    .deletedBy(userId)
                    .otherUserId(otherUserId)
                    .deleteType("FOR_ME")
                    .build();
            messagingTemplate.convertAndSend("/queue/message-deleted/" + userId, dto);
            return dto;
        }
    }

    @Transactional
    public MessageDTO editMessage(Long userId, Long messageId, String newContent) {
        Message m = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        if (!m.getSender().getId().equals(userId)) {
            throw new UnauthorizedException("Can only edit your own messages");
        }
        if (Boolean.TRUE.equals(m.getDeleted())) {
            throw new RuntimeException("Cannot edit a deleted message");
        }
        if (m.getCreatedAt().plusMinutes(15).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot edit after 15 minutes");
        }
        m.setContent(encryption.encrypt(newContent));
        m.setEdited(true);
        m.setEditedAt(LocalDateTime.now());
        messageRepository.save(m);

        MessageDTO dto = mapToDTO(m);
        if (m.getChatRoomId() != null) {
            messagingTemplate.convertAndSend("/topic/room/" + m.getChatRoomId(), dto);
        } else {
            messagingTemplate.convertAndSend("/queue/messages/" + m.getReceiver().getId(), dto);
            messagingTemplate.convertAndSend("/queue/messages/" + m.getSender().getId(), dto);
        }
        return dto;
    }

    @Transactional
    public MessageDTO pinMessage(Long userId, Long messageId) {
        Message m = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        String key = chatKey(m);
        if (pinnedRepository.countByChatKey(key) >= MAX_PINNED_PER_CHAT) {
            throw new RuntimeException("Max " + MAX_PINNED_PER_CHAT + " pinned messages per chat");
        }
        if (pinnedRepository.findByMessageId(messageId).isPresent()) {
            return mapToDTO(m);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        PinnedMessage pin = PinnedMessage.builder()
                .message(m).pinnedBy(user).chatKey(key).build();
        pinnedRepository.save(pin);
        return mapToDTO(m);
    }

    @Transactional
    public void unpinMessage(Long userId, Long messageId) {
        pinnedRepository.deleteByMessageId(messageId);
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getPinned(Long userId, Long otherUserId, Long roomId) {
        String key;
        if (roomId != null) {
            if (!roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
                throw new UnauthorizedException("Not a member");
            }
            key = "ROOM:" + roomId;
        } else {
            key = dmKey(userId, otherUserId);
        }
        return pinnedRepository.findByChatKey(key).stream()
                .map(p -> mapToDTO(p.getMessage()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> searchMessages(Long userId, String q) {
        if (q == null || q.trim().length() < 2) return List.of();
        List<Message> results = messageRepository.searchMessages(userId, q.trim());
        return results.stream()
                .filter(m -> !Boolean.TRUE.equals(m.getDeleted()))
                .filter(m -> m.getHiddenFor() == null || !m.getHiddenFor().equals(userId))
                .map(this::mapToDTO)
                .filter(d -> d.getContent() != null && d.getContent().toLowerCase().contains(q.trim().toLowerCase()))
                .collect(Collectors.toList());
    }

    public void broadcastTyping(Long senderId, Long receiverId, Long roomId, boolean typing) {
        User sender = userRepository.findById(senderId).orElse(null);
        if (sender == null) return;
        TypingDTO dto = TypingDTO.builder()
                .userId(senderId)
                .userName(sender.getName())
                .typing(typing)
                .build();
        if (roomId != null) {
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/typing", dto);
        } else {
            messagingTemplate.convertAndSend("/queue/typing/" + receiverId, dto);
        }
    }

    @Transactional
    public ReactionDTO toggleReaction(Long userId, ReactionRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Message message = messageRepository.findById(req.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (Boolean.TRUE.equals(message.getDeleted())) {
            throw new RuntimeException("Cannot react to a deleted message");
        }

        boolean isRoom = message.getChatRoomId() != null;
        Long senderId = message.getSender().getId();
        Long receiverId = isRoom ? null : message.getReceiver().getId();

        if (isRoom) {
            if (!roomMemberRepository.existsByRoomIdAndUserId(message.getChatRoomId(), userId)) {
                throw new RuntimeException("Not a member");
            }
        } else {
            if (!userId.equals(senderId) && !userId.equals(receiverId)) {
                throw new RuntimeException("You can only react to messages in your conversations");
            }
        }

        Optional<MessageReaction> existing = reactionRepository
                .findByMessageIdAndUserIdAndEmoji(req.getMessageId(), userId, req.getEmoji());

        if (existing.isPresent()) {
            reactionRepository.delete(existing.get());
            broadcastReactionNotification(req, user, "REMOVED", senderId, receiverId, message.getChatRoomId());
            return null;
        } else {
            MessageReaction reaction = MessageReaction.builder()
                    .message(message).user(user).emoji(req.getEmoji()).build();
            MessageReaction saved = reactionRepository.save(reaction);
            broadcastReactionNotification(req, user, "ADDED", senderId, receiverId, message.getChatRoomId());
            return mapReactionToDTO(saved);
        }
    }

    private void broadcastReactionNotification(ReactionRequest req, User user, String action,
                                               Long senderId, Long receiverId, Long roomId) {
        ReactionNotificationDTO n = ReactionNotificationDTO.builder()
                .messageId(req.getMessageId())
                .userId(user.getId())
                .userName(user.getName())
                .emoji(req.getEmoji())
                .action(action)
                .build();
        if (roomId != null) {
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/reactions", n);
        } else {
            messagingTemplate.convertAndSend("/queue/reactions/" + senderId, n);
            if (!senderId.equals(receiverId)) {
                messagingTemplate.convertAndSend("/queue/reactions/" + receiverId, n);
            }
        }
    }

    public List<ReactionDTO> getReactions(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        if (message.getChatRoomId() != null) {
            if (!roomMemberRepository.existsByRoomIdAndUserId(message.getChatRoomId(), userId)) {
                throw new RuntimeException("Not a member");
            }
        } else {
            Long s = message.getSender().getId();
            Long r = message.getReceiver().getId();
            if (!userId.equals(s) && !userId.equals(r)) {
                throw new RuntimeException("Cannot view");
            }
        }
        return reactionRepository.findByMessageId(messageId).stream()
                .map(this::mapReactionToDTO).collect(Collectors.toList());
    }

    private String chatKey(Message m) {
        if (m.getChatRoomId() != null) return "ROOM:" + m.getChatRoomId();
        return dmKey(m.getSender().getId(), m.getReceiver().getId());
    }

    private String dmKey(Long a, Long b) {
        long lo = Math.min(a, b);
        long hi = Math.max(a, b);
        return "DM:" + lo + ":" + hi;
    }

    private String previewOf(Message m) {
        if (m == null) return "";
        if (Boolean.TRUE.equals(m.getDeleted())) return DELETED_MSG;
        if (m.getMessageType() != null) {
            switch (m.getMessageType()) {
                case IMAGE: return "📷 Photo";
                case FILE: return "📎 File";
                case VOICE: return "🎤 Voice message";
                default: break;
            }
        }
        String c = decryptSafe(m.getContent());
        return c.length() > 60 ? c.substring(0, 60) + "..." : c;
    }

    public MessageDTO mapToDTO(Message m) {
        String content;
        if (Boolean.TRUE.equals(m.getDeleted())) {
            content = DELETED_MSG;
        } else {
            content = decryptSafe(m.getContent());
        }

        List<ReactionDTO> reactionDTOs = Collections.emptyList();
        if (m.getReactions() != null && !m.getReactions().isEmpty()) {
            reactionDTOs = m.getReactions().stream()
                    .map(this::mapReactionToDTO)
                    .collect(Collectors.toList());
        }

        List<AttachmentDTO> attachmentDTOs = Collections.emptyList();
        if (m.getAttachments() != null && !m.getAttachments().isEmpty()) {
            attachmentDTOs = m.getAttachments().stream()
                    .map(a -> AttachmentDTO.builder()
                            .id(a.getId())
                            .attachmentType(a.getAttachmentType().name())
                            .url(a.getUrl())
                            .fileName(a.getFileName())
                            .fileSize(a.getFileSize())
                            .durationSeconds(a.getDurationSeconds())
                            .build())
                    .collect(Collectors.toList());
        }

        boolean pinned = false;
        try { pinned = pinnedRepository.findByMessageId(m.getId()).isPresent(); } catch (Exception ignored) {}

        MessageDTO.MessageDTOBuilder builder = MessageDTO.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .senderProfilePic(m.getSender().getProfilePicUrl())
                .receiverId(m.getReceiver() != null ? m.getReceiver().getId() : null)
                .chatRoomId(m.getChatRoomId())
                .content(content)
                .messageType(m.getMessageType() != null ? m.getMessageType().name() : "TEXT")
                .readStatus(Boolean.TRUE.equals(m.getReadStatus()))
                .readAt(m.getReadAt())
                .deleted(Boolean.TRUE.equals(m.getDeleted()))
                .deletedBy(m.getDeletedBy())
                .deleteType(m.getDeleteType())
                .hiddenFor(m.getHiddenFor())
                .edited(Boolean.TRUE.equals(m.getEdited()))
                .editedAt(m.getEditedAt())
                .reactions(reactionDTOs)
                .attachments(attachmentDTOs)
                .pinned(pinned)
                .createdAt(m.getCreatedAt());

        if (m.getReplyTo() != null) {
            Message reply = m.getReplyTo();
            builder.replyToId(reply.getId());
            if (Boolean.TRUE.equals(reply.getDeleted())) {
                builder.replyToContent(DELETED_MSG);
            } else {
                builder.replyToContent(decryptSafe(reply.getContent()));
            }
            builder.replyToSenderName(reply.getSender().getName());
        }

        return builder.build();
    }

    private ReactionDTO mapReactionToDTO(MessageReaction r) {
        return ReactionDTO.builder()
                .id(r.getId())
                .messageId(r.getMessage().getId())
                .userId(r.getUser().getId())
                .userName(r.getUser().getName())
                .emoji(r.getEmoji())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private String decryptSafe(String content) {
        if (content == null) return "";
        try {
            if (encryption.isEncrypted(content)) {
                return encryption.decrypt(content);
            }
            return content;
        } catch (Exception e) {
            return content;
        }
    }
}