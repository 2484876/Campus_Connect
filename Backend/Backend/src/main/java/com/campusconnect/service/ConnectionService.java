package com.campusconnect.service;

import com.campusconnect.dto.ConnectionDTO;
import com.campusconnect.entity.*;
import com.campusconnect.enums.*;
import com.campusconnect.exception.*;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ConnectionDTO sendRequest(Long senderId, Long receiverId) {
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
                .status(ConnectionStatus.PENDING).build();
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

        return mapToDTO(conn, userId);
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

    public Page<ConnectionDTO> getConnections(Long userId, int page, int size) {
        return connectionRepository.findAcceptedConnections(userId, PageRequest.of(page, size))
                .map(conn -> mapToDTO(conn, userId));
    }

    public Page<ConnectionDTO> getPendingRequests(Long userId, int page, int size) {
        return connectionRepository.findPendingRequests(userId, PageRequest.of(page, size))
                .map(conn -> mapToDTO(conn, userId));
    }

    private ConnectionDTO mapToDTO(Connection conn, Long currentUserId) {
        User other = conn.getSender().getId().equals(currentUserId)
                ? conn.getReceiver() : conn.getSender();
        return ConnectionDTO.builder()
                .connectionId(conn.getId())
                .userId(other.getId())
                .userName(other.getName())
                .userProfilePic(other.getProfilePicUrl())
                .userPosition(other.getPosition())
                .userRole(other.getRole().name())
                .userDepartment(other.getDepartment())
                .status(conn.getStatus().name())
                .createdAt(conn.getCreatedAt())
                .build();
    }
}