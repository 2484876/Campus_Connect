package com.campusconnect.service;

import com.campusconnect.dto.*;
import com.campusconnect.entity.*;
import com.campusconnect.enums.*;
import com.campusconnect.exception.*;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired(required = false)
    private AchievementService achievementService;

    @Transactional
    public ConnectionDTO sendRequest(Long senderId, Long receiverId, String message) {
        if (senderId.equals(receiverId)) {
            throw new BadRequestException("Cannot connect with yourself");
        }
        var existing = connectionRepository.findBetweenUsers(senderId, receiverId);
        if (existing.isPresent()) {
            throw new BadRequestException("Connection already exists");
        }
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        Connection conn = Connection.builder()
                .sender(sender).receiver(receiver)
                .status(ConnectionStatus.PENDING)
                .message(message)
                .build();
        Connection saved = connectionRepository.save(conn);

        notificationService.createNotification(
                receiverId, senderId, NotificationType.CONNECTION_REQUEST, saved.getId());

        return mapToDTO(saved, senderId);
    }

    @Transactional
    public ConnectionDTO acceptRequest(Long userId, Long connectionId) {
        Connection conn = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));
        if (!conn.getReceiver().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to accept this request");
        }
        conn.setStatus(ConnectionStatus.ACCEPTED);
        connectionRepository.save(conn);

        notificationService.createNotification(
                conn.getSender().getId(), userId, NotificationType.CONNECTION_ACCEPTED, connectionId);

        try {
            if (achievementService != null) {
                awardConnectionAchievements(userId);
                awardConnectionAchievements(conn.getSender().getId());
            }
        } catch (Exception ignored) {}

        return mapToDTO(conn, userId);
    }

    private void awardConnectionAchievements(Long userId) {
        int count = connectionRepository.countAcceptedConnections(userId);
        if (count >= 1) achievementService.award(userId, "FIRST_CONNECTION");
        if (count >= 10) achievementService.award(userId, "TEN_CONNECTIONS");
        if (count >= 50) achievementService.award(userId, "FIFTY_CONNECTIONS");
    }

    @Transactional
    public void rejectRequest(Long userId, Long connectionId) {
        Connection conn = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));
        if (!conn.getReceiver().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to reject this request");
        }
        conn.setStatus(ConnectionStatus.REJECTED);
        connectionRepository.save(conn);
    }

    @Transactional
    public void removeConnection(Long userId, Long connectionId) {
        Connection conn = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));
        if (!conn.getSender().getId().equals(userId) && !conn.getReceiver().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to remove this connection");
        }
        connectionRepository.delete(conn);
    }

    @Transactional
    public void withdrawRequest(Long userId, Long connectionId) {
        Connection conn = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));
        if (!conn.getSender().getId().equals(userId)) {
            throw new BadRequestException("Only the sender can withdraw a request");
        }
        if (conn.getStatus() != ConnectionStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be withdrawn");
        }
        connectionRepository.delete(conn);
    }

    public Page<ConnectionDTO> getConnections(Long userId, int page, int size) {
        return connectionRepository.findAcceptedConnections(userId, PageRequest.of(page, size))
                .map(conn -> mapToDTO(conn, userId));
    }

    public Page<ConnectionDTO> getPendingRequests(Long userId, int page, int size) {
        return connectionRepository.findPendingRequests(userId, PageRequest.of(page, size))
                .map(conn -> mapToDTO(conn, userId));
    }

    public Page<ConnectionDTO> getSentRequests(Long userId, int page, int size) {
        return connectionRepository.findSentRequests(userId, PageRequest.of(page, size))
                .map(conn -> mapToDTO(conn, userId));
    }

    // ============================================================
    // Mutual connections
    // ============================================================

    public List<MutualConnectionDTO> getMutualConnections(Long currentUserId, Long otherUserId, int limit) {
        if (currentUserId.equals(otherUserId)) return Collections.emptyList();
        List<Long> mutualIds = connectionRepository.findMutualConnectionIds(currentUserId, otherUserId);
        if (mutualIds.isEmpty()) return Collections.emptyList();
        return userRepository.findAllById(mutualIds).stream()
                .limit(limit)
                .map(u -> MutualConnectionDTO.builder()
                        .userId(u.getId())
                        .name(u.getName())
                        .profilePicUrl(u.getProfilePicUrl())
                        .position(u.getPosition())
                        .department(u.getDepartment())
                        .build())
                .collect(Collectors.toList());
    }

    public int getMutualConnectionCount(Long currentUserId, Long otherUserId) {
        if (currentUserId.equals(otherUserId)) return 0;
        return connectionRepository.findMutualConnectionIds(currentUserId, otherUserId).size();
    }

    // ============================================================
    // Smart suggestions - mixed signal scoring
    // ============================================================

    public List<ConnectionSuggestionDTO> getSuggestions(Long userId, int limit) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get everyone I'm already connected with or have a pending/rejected request to
        Set<Long> excludeIds = new HashSet<>();
        excludeIds.add(userId);
        connectionRepository.findAll().forEach(c -> {
            if (c.getSender().getId().equals(userId)) excludeIds.add(c.getReceiver().getId());
            if (c.getReceiver().getId().equals(userId)) excludeIds.add(c.getSender().getId());
        });

        List<Long> myConnIds = connectionRepository.findConnectedUserIds(userId);

        // Score each candidate: department match + mutual count + same role tier
        List<User> candidates = userRepository.findAll().stream()
                .filter(u -> !excludeIds.contains(u.getId()))
                .collect(Collectors.toList());

        List<ConnectionSuggestionDTO> scored = new ArrayList<>();
        for (User u : candidates) {
            int score = 0;
            List<String> reasons = new ArrayList<>();

            
            if (me.getDepartment() != null && me.getDepartment().equalsIgnoreCase(u.getDepartment())) {
                score += 5;
                reasons.add("Same department");
            }


            int mutuals = connectionRepository.findMutualConnectionIds(userId, u.getId()).size();
            if (mutuals > 0) {
                score += Math.min(mutuals * 2, 20);
                reasons.add(mutuals + " mutual " + (mutuals == 1 ? "connection" : "connections"));
            }


            if (me.getRole() != null && u.getRole() != null && me.getRole().equals(u.getRole())) {
                score += 2;
            }



            if (score > 0) {
                scored.add(ConnectionSuggestionDTO.builder()
                        .userId(u.getId())
                        .name(u.getName())
                        .profilePicUrl(u.getProfilePicUrl())
                        .position(u.getPosition())
                        .department(u.getDepartment())
                        .role(u.getRole() != null ? u.getRole().name() : null)
                        .mutualCount(mutuals)
                        .reason(String.join(" · ", reasons))
                        .score(score)
                        .build());
            }
        }

        scored.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return scored.stream().limit(limit).collect(Collectors.toList());
    }



    private ConnectionDTO mapToDTO(Connection conn, Long currentUserId) {
        boolean iAmSender = conn.getSender().getId().equals(currentUserId);
        User other = iAmSender ? conn.getReceiver() : conn.getSender();
        return ConnectionDTO.builder()
                .connectionId(conn.getId())
                .userId(other.getId())
                .userName(other.getName())
                .userProfilePic(other.getProfilePicUrl())
                .userPosition(other.getPosition())
                .userRole(other.getRole() != null ? other.getRole().name() : null)
                .userDepartment(other.getDepartment())
                .status(conn.getStatus().name())
                .message(conn.getMessage())
                .sentByMe(iAmSender)
                .createdAt(conn.getCreatedAt())
                .build();
    }
}