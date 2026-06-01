package com.campusconnect.service;

import com.campusconnect.dto.ChatRoomDTO;
import com.campusconnect.dto.ChatRoomMemberDTO;
import com.campusconnect.dto.CreateRoomRequest;
import com.campusconnect.entity.ChatRoom;
import com.campusconnect.entity.ChatRoomMember;
import com.campusconnect.entity.Message;
import com.campusconnect.entity.User;
import com.campusconnect.enums.ChatMemberRole;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.exception.UnauthorizedException;
import com.campusconnect.repository.ChatRoomMemberRepository;
import com.campusconnect.repository.ChatRoomRepository;
import com.campusconnect.repository.MessageRepository;
import com.campusconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository roomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final PresenceService presenceService;

    @Transactional
    public ChatRoomDTO createRoom(Long creatorId, CreateRoomRequest req) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatRoom room = ChatRoom.builder()
                .name(req.getName())
                .avatarUrl(req.getAvatarUrl())
                .createdBy(creator)
                .isActive(true)
                .build();
        room = roomRepository.save(room);

        Set<Long> memberIds = new HashSet<>(req.getMemberIds());
        memberIds.add(creatorId);

        LocalDateTime now = LocalDateTime.now();
        for (Long uid : memberIds) {
            User u = userRepository.findById(uid).orElse(null);
            if (u == null) continue;
            ChatRoomMember m = ChatRoomMember.builder()
                    .room(room)
                    .user(u)
                    .memberRole(uid.equals(creatorId) ? ChatMemberRole.ADMIN : ChatMemberRole.MEMBER)
                    .lastReadAt(now)
                    .build();
            memberRepository.save(m);
        }

        return toDTO(room, creatorId, true);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getMyRooms(Long userId) {
        List<ChatRoom> rooms = roomRepository.findRoomsForUser(userId);
        return rooms.stream().map(r -> toDTO(r, userId, false)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatRoomDTO getRoom(Long roomId, Long userId) {
        ChatRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        if (!memberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new UnauthorizedException("Not a member of this room");
        }
        return toDTO(room, userId, true);
    }

    @Transactional
    public ChatRoomDTO addMembers(Long roomId, Long actorId, List<Long> userIds) {
        requireAdmin(roomId, actorId);
        ChatRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        for (Long uid : userIds) {
            if (memberRepository.existsByRoomIdAndUserId(roomId, uid)) continue;
            User u = userRepository.findById(uid).orElse(null);
            if (u == null) continue;
            ChatRoomMember m = ChatRoomMember.builder()
                    .room(room).user(u).memberRole(ChatMemberRole.MEMBER)
                    .lastReadAt(LocalDateTime.now()).build();
            memberRepository.save(m);
        }
        return toDTO(room, actorId, true);
    }

    @Transactional
    public void removeMember(Long roomId, Long actorId, Long targetUserId) {
        if (!actorId.equals(targetUserId)) {
            requireAdmin(roomId, actorId);
        }
        memberRepository.deleteByRoomIdAndUserId(roomId, targetUserId);
    }

    @Transactional
    public void renameRoom(Long roomId, Long actorId, String name) {
        requireAdmin(roomId, actorId);
        ChatRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        room.setName(name);
        roomRepository.save(room);
    }

    @Transactional
    public void markRoomRead(Long roomId, Long userId) {
        memberRepository.updateLastRead(roomId, userId, LocalDateTime.now());
    }

    private void requireAdmin(Long roomId, Long userId) {
        ChatRoomMember m = memberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new UnauthorizedException("Not a member"));
        if (m.getMemberRole() != ChatMemberRole.ADMIN) {
            throw new UnauthorizedException("Admin only");
        }
    }

    public ChatRoomDTO toDTO(ChatRoom room, Long viewerId, boolean includeMembers) {
        List<ChatRoomMember> members = memberRepository.findByRoomId(room.getId());

        ChatRoomMember mySelf = members.stream()
                .filter(m -> m.getUser().getId().equals(viewerId))
                .findFirst().orElse(null);

        LocalDateTime lastRead = mySelf != null && mySelf.getLastReadAt() != null
                ? mySelf.getLastReadAt() : LocalDateTime.now().minusYears(10);

        Message last = messageRepository.findLastRoomMessage(room.getId());
        int unread = messageRepository.countRoomUnreadAfter(room.getId(), viewerId, lastRead);

        ChatRoomDTO.ChatRoomDTOBuilder b = ChatRoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .avatarUrl(room.getAvatarUrl())
                .createdById(room.getCreatedBy().getId())
                .createdByName(room.getCreatedBy().getName())
                .createdAt(room.getCreatedAt())
                .memberCount(members.size())
                .unreadCount(unread)
                .myRole(mySelf != null ? mySelf.getMemberRole().name() : "MEMBER")
                .lastMessageAt(last != null ? last.getCreatedAt() : room.getCreatedAt())
                .lastMessagePreview(last != null ? previewOf(last) : "")
                .lastActivity(last != null ? last.getCreatedAt() : room.getCreatedAt());

        if (includeMembers) {
            List<Long> uids = members.stream().map(m -> m.getUser().getId()).collect(Collectors.toList());
            Map<Long, String> presenceMap = presenceService.bulk(uids).stream()
                    .collect(Collectors.toMap(p -> p.getUserId(), p -> p.getStatus()));

            List<ChatRoomMemberDTO> memberDtos = members.stream().map(m -> ChatRoomMemberDTO.builder()
                    .userId(m.getUser().getId())
                    .name(m.getUser().getName())
                    .profilePicUrl(m.getUser().getProfilePicUrl())
                    .position(m.getUser().getPosition())
                    .role(m.getMemberRole().name())
                    .presence(presenceMap.getOrDefault(m.getUser().getId(), "OFFLINE"))
                    .build()).collect(Collectors.toList());
            b.members(memberDtos);
        }

        return b.build();
    }

    private String previewOf(Message m) {
        if (m.getDeleted() != null && m.getDeleted()) return "(deleted)";
        if (m.getMessageType() != null) {
            switch (m.getMessageType()) {
                case IMAGE: return "📷 Photo";
                case FILE: return "📎 File";
                case VOICE: return "🎤 Voice message";
                default: break;
            }
        }
        String c = m.getContent() == null ? "" : m.getContent();
        return c.length() > 60 ? c.substring(0, 60) + "..." : c;
    }
}