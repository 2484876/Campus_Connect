package com.campusconnect.service;

import com.campusconnect.dto.AdminActionLogDTO;
import com.campusconnect.dto.AdminContentDTO;
import com.campusconnect.dto.AdminStatsDTO;
import com.campusconnect.dto.AdminUserDTO;
import com.campusconnect.entity.AdminActionLog;
import com.campusconnect.entity.Comment;
import com.campusconnect.entity.CommunityPost;
import com.campusconnect.entity.Event;
import com.campusconnect.entity.Post;
import com.campusconnect.entity.Report;
import com.campusconnect.entity.User;
import com.campusconnect.enums.Role;
import com.campusconnect.exception.BadRequestException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.AdminActionLogRepository;
import com.campusconnect.repository.CommentRepository;
import com.campusconnect.repository.CommunityPostRepository;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.PostRepository;
import com.campusconnect.repository.ReportRepository;
import com.campusconnect.repository.StoryRepository;
import com.campusconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final EventRepository eventRepository;
    private final StoryRepository storyRepository;
    private final ReportRepository reportRepository;
    private final AdminActionLogRepository logRepository;

    public AdminStatsDTO getStats() {
        long total = userRepository.count();
        long active = userRepository.countByIsActiveTrue();
        return AdminStatsDTO.builder()
                .totalUsers(total)
                .activeUsers(active)
                .suspendedUsers(total - active)
                .adminCount(userRepository.countByRole(Role.ADMIN))
                .totalPosts(postRepository.count())
                .activePosts(postRepository.countByIsActiveTrue())
                .pendingReports(reportRepository.countByStatus("PENDING"))
                .newUsersLast7Days(userRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(7)))
                .build();
    }

    public Page<AdminUserDTO> listUsers(String q, String status, String role, int page, int size) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Boolean active = null;
        if ("ACTIVE".equalsIgnoreCase(status)) active = Boolean.TRUE;
        else if ("SUSPENDED".equalsIgnoreCase(status)) active = Boolean.FALSE;
        Role roleFilter = null;
        if (role != null && !role.isBlank()) {
            try { roleFilter = Role.valueOf(role.trim().toUpperCase()); }
            catch (IllegalArgumentException ex) { throw new BadRequestException("Unknown role: " + role); }
        }
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.adminSearch(query, active, roleFilter, pageable).map(this::toUserDTO);
    }

    public AdminUserDTO getUser(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toUserDTO(u);
    }

    @Transactional
    public void setUserStatus(Long adminId, Long targetId, boolean active, String reason) {
        User target = loadTarget(adminId, targetId);
        if (!active && isLastActiveAdmin(target)) {
            throw new BadRequestException("Cannot suspend the last active admin");
        }
        target.setActive(active);
        userRepository.save(target);
        log(adminId, active ? "REACTIVATE_USER" : "SUSPEND_USER", "USER",
                target.getId(), target.getName(), reason);
    }

    @Transactional
    public void setUserRole(Long adminId, Long targetId, String roleStr) {
        User target = loadTarget(adminId, targetId);
        Role newRole;
        try { newRole = Role.valueOf(roleStr.trim().toUpperCase()); }
        catch (Exception ex) { throw new BadRequestException("Unknown role: " + roleStr); }
        if (target.getRole() == Role.ADMIN && newRole != Role.ADMIN && isLastActiveAdmin(target)) {
            throw new BadRequestException("Cannot demote the last active admin");
        }
        String previous = target.getRole().name();
        target.setRole(newRole);
        userRepository.save(target);
        log(adminId, "CHANGE_ROLE", "USER", target.getId(), target.getName(),
                previous + " -> " + newRole.name());
    }

    @Transactional
    public void deleteUser(Long adminId, Long targetId, String mode) {
        User target = loadTarget(adminId, targetId);
        if (isLastActiveAdmin(target)) {
            throw new BadRequestException("Cannot delete the last active admin");
        }
        if ("HARD".equalsIgnoreCase(mode)) {
            String label = target.getName();
            try {
                userRepository.delete(target);
                userRepository.flush();
            } catch (Exception ex) {
                throw new BadRequestException("Hard delete blocked by linked records. Use soft delete instead.");
            }
            log(adminId, "HARD_DELETE_USER", "USER", targetId, label, null);
            return;
        }
        String label = target.getName();
        target.setActive(false);
        target.setName("Deleted User");
        target.setEmail("deleted_" + target.getId() + "@deleted.local");
        target.setBio(null);
        target.setProfilePicUrl(null);
        target.setBannerUrl(null);
        target.setPhone(null);
        userRepository.save(target);
        log(adminId, "SOFT_DELETE_USER", "USER", targetId, label, null);
    }

    public Page<AdminContentDTO> listPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findAll(pageable).map(this::postToDTO);
    }

    @Transactional
    public void removeContent(Long adminId, String type, Long id) {
        String key = type == null ? "" : type.toUpperCase();
        switch (key) {
            case "POST" -> {
                Post p = postRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
                p.setActive(false);
                postRepository.save(p);
            }
            case "COMMENT" -> {
                Comment c = commentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
                c.setActive(false);
                commentRepository.save(c);
            }
            case "COMMUNITY_POST" -> {
                CommunityPost cp = communityPostRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Community post not found"));
                cp.setActive(false);
                communityPostRepository.save(cp);
            }
            case "EVENT" -> {
                Event e = eventRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
                e.setActive(false);
                eventRepository.save(e);
            }
            case "STORY" -> {
                if (!storyRepository.existsById(id)) {
                    throw new ResourceNotFoundException("Story not found");
                }
                storyRepository.deleteById(id);
            }
            default -> throw new BadRequestException("Unsupported content type: " + type);
        }
        log(adminId, "REMOVE_CONTENT", key, id, null, null);
    }

    public Page<com.campusconnect.dto.ReportDTO> listReports(int page, int size) {
        return reportRepository.findByStatusOrderByCreatedAtDesc("PENDING", PageRequest.of(page, size))
                .map(this::reportToDTO);
    }

    @Transactional
    public void resolveReport(Long adminId, Long reportId, String status) {
        Report r = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        r.setStatus(status);
        r.setReviewedAt(LocalDateTime.now());
        reportRepository.save(r);
        log(adminId, "RESOLVE_REPORT", "REPORT", reportId, null, status);
    }

    public Page<AdminActionLogDTO> listLogs(int page, int size) {
        return logRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)).map(this::logToDTO);
    }

    private User loadTarget(Long adminId, Long targetId) {
        if (adminId.equals(targetId)) {
            throw new BadRequestException("You cannot perform this action on your own account");
        }
        return userRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isLastActiveAdmin(User target) {
        return target.getRole() == Role.ADMIN
                && target.isActive()
                && userRepository.countByRoleAndIsActiveTrue(Role.ADMIN) <= 1;
    }

    private void log(Long adminId, String action, String targetType, Long targetId, String label, String details) {
        String adminName = userRepository.findById(adminId).map(User::getName).orElse("Unknown");
        logRepository.save(AdminActionLog.builder()
                .adminId(adminId)
                .adminName(adminName)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .targetLabel(label)
                .details(details)
                .build());
    }

    private AdminUserDTO toUserDTO(User u) {
        return AdminUserDTO.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole().name())
                .department(u.getDepartment())
                .position(u.getPosition())
                .profilePicUrl(u.getProfilePicUrl())
                .active(u.isActive())
                .createdAt(u.getCreatedAt())
                .postCount(postRepository.countByUserIdAndIsActiveTrue(u.getId()))
                .build();
    }

    private AdminContentDTO postToDTO(Post p) {
        String content = p.getContent() == null ? "" : p.getContent();
        String preview = content.length() > 140 ? content.substring(0, 140) + "..." : content;
        return AdminContentDTO.builder()
                .id(p.getId())
                .type("POST")
                .authorId(p.getUser().getId())
                .authorName(p.getUser().getName())
                .preview(preview)
                .active(p.isActive())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private com.campusconnect.dto.ReportDTO reportToDTO(Report r) {
        return com.campusconnect.dto.ReportDTO.builder()
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

    private AdminActionLogDTO logToDTO(AdminActionLog l) {
        return AdminActionLogDTO.builder()
                .id(l.getId())
                .adminId(l.getAdminId())
                .adminName(l.getAdminName())
                .action(l.getAction())
                .targetType(l.getTargetType())
                .targetId(l.getTargetId())
                .targetLabel(l.getTargetLabel())
                .details(l.getDetails())
                .createdAt(l.getCreatedAt())
                .build();
    }
}