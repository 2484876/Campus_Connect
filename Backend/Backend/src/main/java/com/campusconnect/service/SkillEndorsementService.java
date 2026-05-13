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
public class SkillEndorsementService {

    private final SkillEndorsementRepository endorsementRepository;
    private final UserRepository userRepository;
    private final ConnectionRepository connectionRepository;
    private final NotificationService notificationService;

    @Autowired(required = false)
    private AchievementService achievementService;

    @Transactional
    public SkillEndorsementDTO endorse(Long endorserId, CreateEndorsementRequest req) {
        Long endorseeId = req.getEndorseeId();
        if (endorseeId == null) throw new BadRequestException("endorseeId is required");
        if (endorserId.equals(endorseeId)) throw new BadRequestException("Cannot endorse yourself");

        // Must be connected (LinkedIn-style)
        var existingConn = connectionRepository.findBetweenUsers(endorserId, endorseeId);
        if (existingConn.isEmpty() || existingConn.get().getStatus() != ConnectionStatus.ACCEPTED) {
            throw new BadRequestException("You can only endorse your connections");
        }

        boolean hasSkill = req.getSkill() != null && !req.getSkill().isBlank();
        boolean hasCategory = req.getCategory() != null && !req.getCategory().isBlank();
        if (!hasSkill && !hasCategory) {
            throw new BadRequestException("Provide either a skill or a category");
        }

        EndorsementCategory categoryEnum = null;
        if (hasCategory) {
            try { categoryEnum = EndorsementCategory.valueOf(req.getCategory().toUpperCase()); }
            catch (IllegalArgumentException ex) { throw new BadRequestException("Invalid category"); }
        }

        String normSkill = hasSkill ? req.getSkill().trim() : null;

        // Prevent duplicate
        var dupe = endorsementRepository.findExactMatch(endorseeId, endorserId, normSkill, categoryEnum);
        if (dupe.isPresent()) {
            throw new BadRequestException(hasSkill ? "You've already endorsed this skill" : "You've already endorsed this category");
        }

        User endorser = userRepository.findById(endorserId)
                .orElseThrow(() -> new ResourceNotFoundException("Endorser not found"));
        User endorsee = userRepository.findById(endorseeId)
                .orElseThrow(() -> new ResourceNotFoundException("Endorsee not found"));

        SkillEndorsement e = SkillEndorsement.builder()
                .endorser(endorser).endorsee(endorsee)
                .skill(normSkill).category(categoryEnum)
                .message(req.getMessage())
                .build();
        SkillEndorsement saved = endorsementRepository.save(e);

        notificationService.createNotification(endorseeId, endorserId, NotificationType.ENDORSEMENT, saved.getId());

        try {
            if (achievementService != null) {
                achievementService.award(endorserId, "FIRST_ENDORSEMENT_GIVEN");
                long received = endorsementRepository.countByEndorseeId(endorseeId);
                if (received >= 10) achievementService.award(endorseeId, "TEN_ENDORSEMENTS_RECEIVED");
            }
        } catch (Exception ignored) {}

        return mapToDTO(saved);
    }

    @Transactional
    public void removeEndorsement(Long endorserId, Long endorsementId) {
        SkillEndorsement e = endorsementRepository.findById(endorsementId)
                .orElseThrow(() -> new ResourceNotFoundException("Endorsement not found"));
        if (!e.getEndorser().getId().equals(endorserId)) {
            throw new BadRequestException("Not authorized to remove this endorsement");
        }
        endorsementRepository.delete(e);
    }

    public Page<SkillEndorsementDTO> getEndorsementsReceivedBy(Long userId, int page, int size) {
        return endorsementRepository.findByEndorseeIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::mapToDTO);
    }

    public List<SkillEndorsementDTO> getEndorsementsByEndorser(Long endorseeId, Long endorserId) {
        return endorsementRepository.findByEndorseeIdAndEndorserId(endorseeId, endorserId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<SkillEndorsementDTO> getEndorsersForSkill(Long userId, String skill) {
        return endorsementRepository.findEndorsersForSkill(userId, skill).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EndorsementSummaryDTO getSummary(Long userId) {
        long total = endorsementRepository.countByEndorseeId(userId);
        long given = endorsementRepository.countByEndorserId(userId);

        List<EndorsementSummaryDTO.SkillCount> topSkills = endorsementRepository.countSkillsForUser(userId).stream()
                .limit(10)
                .map(row -> EndorsementSummaryDTO.SkillCount.builder()
                        .label((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<EndorsementSummaryDTO.SkillCount> categories = endorsementRepository.countCategoriesForUser(userId).stream()
                .map(row -> EndorsementSummaryDTO.SkillCount.builder()
                        .label(row[0].toString())
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        return EndorsementSummaryDTO.builder()
                .totalReceived(total)
                .totalGiven(given)
                .topSkills(topSkills)
                .categoryBreakdown(categories)
                .build();
    }

    private SkillEndorsementDTO mapToDTO(SkillEndorsement e) {
        return SkillEndorsementDTO.builder()
                .id(e.getId())
                .endorserId(e.getEndorser().getId())
                .endorserName(e.getEndorser().getName())
                .endorserProfilePic(e.getEndorser().getProfilePicUrl())
                .endorserPosition(e.getEndorser().getPosition())
                .endorseeId(e.getEndorsee().getId())
                .endorseeName(e.getEndorsee().getName())
                .skill(e.getSkill())
                .category(e.getCategory() != null ? e.getCategory().name() : null)
                .message(e.getMessage())
                .createdAt(e.getCreatedAt())
                .build();
    }
}