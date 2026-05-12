package com.campusconnect.service;

import com.campusconnect.dto.CreateKudosRequest;
import com.campusconnect.dto.KudosDTO;
import com.campusconnect.entity.Kudos;
import com.campusconnect.entity.User;
import com.campusconnect.enums.NotificationType;
import com.campusconnect.exception.BadRequestException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.KudosRepository;
import com.campusconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KudosService {

    private final KudosRepository kudosRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired(required = false)
    private AchievementService achievementService;

    @Transactional
    public KudosDTO give(Long giverId, CreateKudosRequest req) {
        if (giverId.equals(req.getReceiverId()))
            throw new BadRequestException("You cannot give kudos to yourself");
        User giver = userRepository.findById(giverId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User receiver = userRepository.findById(req.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));
        Kudos k = Kudos.builder()
                .giver(giver).receiver(receiver)
                .category(req.getCategory())
                .message(req.getMessage())
                .isPublic(req.isPublic())
                .build();
        Kudos saved = kudosRepository.save(k);

        try {
            notificationService.createNotification(receiver.getId(), giver.getId(), NotificationType.LIKE, saved.getId());
        } catch (Exception ignored) {}

        try {
            if (achievementService != null) {
                long givenCount = kudosRepository.countByGiverId(giverId);
                if (givenCount == 1) achievementService.award(giverId, "FIRST_KUDOS_GIVEN");

                long receivedCount = kudosRepository.countByReceiverIdAndIsPublicTrue(req.getReceiverId());
                if (receivedCount == 1) achievementService.award(req.getReceiverId(), "FIRST_KUDOS_RECEIVED");
                if (receivedCount >= 10) achievementService.award(req.getReceiverId(), "TEN_KUDOS_RECEIVED");
            }
        } catch (Exception ignored) {}

        return toDTO(saved);
    }

    public Page<KudosDTO> getReceived(Long userId, int page, int size) {
        return kudosRepository.findReceivedPublic(userId, PageRequest.of(page, size)).map(this::toDTO);
    }

    public Page<KudosDTO> getGiven(Long userId, int page, int size) {
        return kudosRepository.findGiven(userId, PageRequest.of(page, size)).map(this::toDTO);
    }

    public Page<KudosDTO> getRecent(int page, int size) {
        return kudosRepository.findRecentPublic(PageRequest.of(page, size)).map(this::toDTO);
    }

    public Map<String, Long> getStats(Long userId) {
        return Map.of(
                "received", kudosRepository.countByReceiverIdAndIsPublicTrue(userId),
                "given", kudosRepository.countByGiverId(userId)
        );
    }

    private KudosDTO toDTO(Kudos k) {
        return KudosDTO.builder()
                .id(k.getId())
                .giverId(k.getGiver().getId())
                .giverName(k.getGiver().getName())
                .giverProfilePic(k.getGiver().getProfilePicUrl())
                .giverPosition(k.getGiver().getPosition())
                .receiverId(k.getReceiver().getId())
                .receiverName(k.getReceiver().getName())
                .receiverProfilePic(k.getReceiver().getProfilePicUrl())
                .receiverPosition(k.getReceiver().getPosition())
                .category(k.getCategory())
                .message(k.getMessage())
                .isPublic(k.isPublic())
                .createdAt(k.getCreatedAt())
                .build();
    }
}