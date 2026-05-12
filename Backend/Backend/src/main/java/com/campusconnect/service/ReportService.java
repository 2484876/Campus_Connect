package com.campusconnect.service;

import com.campusconnect.dto.ReportDTO;
import com.campusconnect.dto.ReportRequest;
import com.campusconnect.entity.Report;
import com.campusconnect.entity.User;
import com.campusconnect.entity.UserBlock;
import com.campusconnect.exception.BadRequestException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.ReportRepository;
import com.campusconnect.repository.UserBlockRepository;
import com.campusconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserBlockRepository blockRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportDTO submitReport(Long reporterId, ReportRequest req) {
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, req.getTargetType(), req.getTargetId())) {
            throw new BadRequestException("You already reported this");
        }
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Report r = Report.builder()
                .reporter(reporter)
                .targetType(req.getTargetType())
                .targetId(req.getTargetId())
                .reason(req.getReason())
                .details(req.getDetails())
                .status("PENDING")
                .build();
        return toDTO(reportRepository.save(r));
    }

    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) throw new BadRequestException("Cannot block yourself");
        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) return;
        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        blockRepository.save(UserBlock.builder().blocker(blocker).blocked(blocked).build());
    }

    @Transactional
    public void unblockUser(Long blockerId, Long blockedId) {
        blockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    public boolean isBlocked(Long blockerId, Long blockedId) {
        return blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    public Page<ReportDTO> getPendingReports(int page, int size) {
        return reportRepository.findByStatusOrderByCreatedAtDesc("PENDING", PageRequest.of(page, size))
                .map(this::toDTO);
    }

    @Transactional
    public void resolveReport(Long reportId, String status) {
        Report r = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        r.setStatus(status);
        r.setReviewedAt(LocalDateTime.now());
        reportRepository.save(r);
    }

    private ReportDTO toDTO(Report r) {
        return ReportDTO.builder()
                .id(r.getId())
                .reporterId(r.getReporter().getId())
                .reporterName(r.getReporter().getName())
                .targetType(r.getTargetType())
                .targetId(r.getTargetId())
                .reason(r.getReason())
                .details(r.getDetails())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}