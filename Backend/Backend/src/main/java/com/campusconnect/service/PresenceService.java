package com.campusconnect.service;

import com.campusconnect.dto.PresenceDTO;
import com.campusconnect.entity.UserPresence;
import com.campusconnect.enums.PresenceStatus;
import com.campusconnect.repository.UserPresenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final UserPresenceRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void setStatus(Long userId, PresenceStatus status) {
        UserPresence p = repository.findById(userId).orElseGet(() -> {
            UserPresence fresh = new UserPresence();
            fresh.setUserId(userId);
            return fresh;
        });
        p.setStatus(status);
        p.setLastSeen(LocalDateTime.now());
        repository.save(p);

        try {
            messagingTemplate.convertAndSend("/topic/presence",
                    PresenceDTO.builder()
                            .userId(userId)
                            .status(status.name())
                            .lastSeen(p.getLastSeen())
                            .build());
        } catch (Exception ignored) {}
    }

    @Transactional(readOnly = true)
    public PresenceDTO getPresence(Long userId) {
        return repository.findById(userId)
                .map(this::toDTO)
                .orElseGet(() -> PresenceDTO.builder()
                        .userId(userId).status(PresenceStatus.OFFLINE.name()).build());
    }

    @Transactional(readOnly = true)
    public List<PresenceDTO> bulk(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        Map<Long, UserPresence> map = repository.findByUserIds(userIds).stream()
                .collect(Collectors.toMap(UserPresence::getUserId, p -> p));
        return userIds.stream()
                .map(id -> map.containsKey(id)
                        ? toDTO(map.get(id))
                        : PresenceDTO.builder().userId(id).status(PresenceStatus.OFFLINE.name()).build())
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void markStaleOffline() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(3);
        List<UserPresence> online = repository.findByStatus(PresenceStatus.ONLINE);
        for (UserPresence p : online) {
            if (p.getLastSeen() != null && p.getLastSeen().isBefore(cutoff)) {
                p.setStatus(PresenceStatus.OFFLINE);
                repository.save(p);
                try {
                    messagingTemplate.convertAndSend("/topic/presence",
                            PresenceDTO.builder()
                                    .userId(p.getUserId())
                                    .status(PresenceStatus.OFFLINE.name())
                                    .lastSeen(p.getLastSeen())
                                    .build());
                } catch (Exception ignored) {}
            }
        }
    }

    private PresenceDTO toDTO(UserPresence p) {
        return PresenceDTO.builder()
                .userId(p.getUserId())
                .status(p.getStatus().name())
                .lastSeen(p.getLastSeen())
                .build();
    }
}