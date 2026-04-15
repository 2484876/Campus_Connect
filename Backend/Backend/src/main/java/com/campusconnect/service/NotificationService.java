package com.campusconnect.service;

import com.campusconnect.dto.NotificationDTO;
import com.campusconnect.entity.*;
import com.campusconnect.enums.NotificationType;
import com.campusconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createNotification(Long userId, Long actorId, NotificationType type, Long referenceId) {
        User user = userRepository.findById(userId).orElse(null);
        User actor = userRepository.findById(actorId).orElse(null);
        if (user == null || actor == null) return;

        Notification notif = Notification.builder()
                .user(user).actor(actor).type(type)
                .referenceId(referenceId).isRead(false).build();
        Notification saved = notificationRepository.save(notif);
        NotificationDTO dto = mapToDTO(saved);

        messagingTemplate.convertAndSend("/queue/notifications/" + userId, dto);
    }

    public Page<NotificationDTO> getNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUserId(userId, PageRequest.of(page, size))
                .map(this::mapToDTO);
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }

    private NotificationDTO mapToDTO(Notification n) {
        String message = switch (n.getType()) {
            case LIKE -> n.getActor().getName() + " liked your post";
            case COMMENT -> n.getActor().getName() + " commented on your post";
            case CONNECTION_REQUEST -> n.getActor().getName() + " sent you a connection request";
            case CONNECTION_ACCEPTED -> n.getActor().getName() + " accepted your connection request";
            case MESSAGE -> n.getActor().getName() + " sent you a message";
            case POST -> n.getActor().getName() + " created a new post";
        };
        return NotificationDTO.builder()
                .id(n.getId()).type(n.getType().name())
                .actorId(n.getActor().getId()).actorName(n.getActor().getName())
                .actorProfilePic(n.getActor().getProfilePicUrl())
                .referenceId(n.getReferenceId()).isRead(n.isRead())
                .createdAt(n.getCreatedAt()).message(message).build();
    }
}