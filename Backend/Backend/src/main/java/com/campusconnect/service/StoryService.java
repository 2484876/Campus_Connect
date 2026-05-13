package com.campusconnect.service;

import com.campusconnect.dto.*;
import com.campusconnect.entity.Story;
import com.campusconnect.entity.StoryView;
import com.campusconnect.entity.User;
import com.campusconnect.exception.BadRequestException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.StoryRepository;
import com.campusconnect.repository.StoryViewRepository;
import com.campusconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

    private static final Logger log = LoggerFactory.getLogger(StoryService.class);

    private final StoryRepository storyRepository;
    private final StoryViewRepository viewRepository;
    private final UserRepository userRepository;

    @Transactional
    public StoryDTO create(Long userId, CreateStoryRequest req) {
        if ((req.getMediaUrl() == null || req.getMediaUrl().isBlank())
                && (req.getCaption() == null || req.getCaption().isBlank())) {
            throw new BadRequestException("Story needs either media or caption");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Story s = Story.builder()
                .user(user)
                .mediaUrl(req.getMediaUrl())
                .caption(req.getCaption())
                .backgroundColor(req.getBackgroundColor())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        return toDTO(storyRepository.save(s), userId);
    }

    public List<UserStoriesGroupDTO> getFeed(Long userId) {
        List<Story> all = storyRepository.findActiveForUser(userId, LocalDateTime.now());
        Map<Long, List<Story>> grouped = new LinkedHashMap<>();
        for (Story s : all) {
            grouped.computeIfAbsent(s.getUser().getId(), k -> new ArrayList<>()).add(s);
        }

        List<UserStoriesGroupDTO> groups = new ArrayList<>();
        for (Map.Entry<Long, List<Story>> entry : grouped.entrySet()) {
            List<StoryDTO> dtos = entry.getValue().stream()
                    .map(s -> toDTO(s, userId)).collect(Collectors.toList());
            boolean allViewed = dtos.stream().allMatch(StoryDTO::isViewedByMe);
            User author = entry.getValue().get(0).getUser();
            groups.add(UserStoriesGroupDTO.builder()
                    .userId(author.getId())
                    .userName(author.getName())
                    .userProfilePic(author.getProfilePicUrl())
                    .allViewed(allViewed)
                    .stories(dtos)
                    .build());
        }

        groups.sort((a, b) -> {
            if (a.getUserId().equals(userId)) return -1;
            if (b.getUserId().equals(userId)) return 1;
            if (a.isAllViewed() == b.isAllViewed()) return 0;
            return a.isAllViewed() ? 1 : -1;
        });

        return groups;
    }

    @Transactional
    public void markViewed(Long storyId, Long viewerId) {
        if (viewRepository.existsByStoryIdAndViewerId(storyId, viewerId)) return;
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found"));
        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        viewRepository.save(StoryView.builder().story(story).viewer(viewer).build());
    }

    @Transactional
    public void delete(Long storyId, Long userId) {
        Story s = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found"));
        if (!s.getUser().getId().equals(userId)) throw new BadRequestException("Not your story");
        // delete this story's views first to avoid FK violation
        // (works whether or not the FK has ON DELETE CASCADE)
        viewRepository.deleteByStoryId(storyId);
        storyRepository.delete(s);
    }

    public List<Map<String, Object>> getViewers(Long storyId, Long ownerId) {
        Story s = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found"));
        if (!s.getUser().getId().equals(ownerId)) throw new BadRequestException("Not your story");
        return viewRepository.findByStoryIdOrderByViewedAtDesc(storyId).stream().map(v -> {
            Map<String, Object> m = new HashMap<>();
            m.put("userId", v.getViewer().getId());
            m.put("userName", v.getViewer().getName());
            m.put("userProfilePic", v.getViewer().getProfilePicUrl());
            m.put("viewedAt", v.getViewedAt());
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * Runs hourly. Deletes story_views for expired stories first, then the stories.
     * Uses REQUIRES_NEW so even if a swallowed error left transaction marked rollback-only,
     * we get a fresh transaction next tick.
     */
    @Scheduled(fixedDelay = 3600000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanupExpired() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int viewsDeleted = viewRepository.deleteViewsOfExpiredStories(now);
            int storiesDeleted = storyRepository.deleteExpired(now);
            if (storiesDeleted > 0) {
                log.info("Story cleanup: removed {} stories and {} views", storiesDeleted, viewsDeleted);
            }
        } catch (Exception e) {
            log.warn("Story cleanup failed: {}", e.getMessage());
        }
    }

    private StoryDTO toDTO(Story s, Long viewerId) {
        boolean viewed = viewerId != null && viewRepository.existsByStoryIdAndViewerId(s.getId(), viewerId);
        long count = viewRepository.countByStoryId(s.getId());
        return StoryDTO.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .userName(s.getUser().getName())
                .userProfilePic(s.getUser().getProfilePicUrl())
                .mediaUrl(s.getMediaUrl())
                .caption(s.getCaption())
                .backgroundColor(s.getBackgroundColor())
                .createdAt(s.getCreatedAt())
                .expiresAt(s.getExpiresAt())
                .viewCount(count)
                .viewedByMe(viewed)
                .build();
    }
}