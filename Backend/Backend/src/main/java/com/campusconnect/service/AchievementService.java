package com.campusconnect.service;

import com.campusconnect.dto.AchievementDTO;
import com.campusconnect.entity.Achievement;
import com.campusconnect.entity.User;
import com.campusconnect.entity.UserAchievement;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.AchievementRepository;
import com.campusconnect.repository.UserAchievementRepository;
import com.campusconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean award(Long userId, String code) {
        if (userAchievementRepository.existsByUserIdAndAchievementCode(userId, code)) return false;
        Achievement ach = achievementRepository.findByCode(code).orElse(null);
        if (ach == null) return false;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        userAchievementRepository.save(UserAchievement.builder().user(user).achievement(ach).build());
        return true;
    }

    public List<AchievementDTO> getMyAchievements(Long userId) {
        Map<String, UserAchievement> earnedMap = userAchievementRepository.findByUserIdOrdered(userId)
                .stream().collect(Collectors.toMap(
                        ua -> ua.getAchievement().getCode(), ua -> ua, (a, b) -> a, LinkedHashMap::new));

        List<Achievement> all = achievementRepository.findAll();
        all.sort(Comparator.comparing(Achievement::getId));

        return all.stream().map(a -> {
            UserAchievement ua = earnedMap.get(a.getCode());
            return AchievementDTO.builder()
                    .id(a.getId()).code(a.getCode()).name(a.getName())
                    .description(a.getDescription()).icon(a.getIcon())
                    .tier(a.getTier()).points(a.getPoints())
                    .earned(ua != null)
                    .earnedAt(ua != null ? ua.getEarnedAt() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<AchievementDTO> getEarned(Long userId) {
        return userAchievementRepository.findByUserIdOrdered(userId).stream().map(ua -> {
            Achievement a = ua.getAchievement();
            return AchievementDTO.builder()
                    .id(a.getId()).code(a.getCode()).name(a.getName())
                    .description(a.getDescription()).icon(a.getIcon())
                    .tier(a.getTier()).points(a.getPoints())
                    .earned(true).earnedAt(ua.getEarnedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getStats(Long userId) {
        List<UserAchievement> earned = userAchievementRepository.findByUserIdOrdered(userId);
        int totalPoints = earned.stream().mapToInt(ua -> ua.getAchievement().getPoints()).sum();
        long totalAvailable = achievementRepository.count();
        Map<String, Object> stats = new HashMap<>();
        stats.put("earnedCount", earned.size());
        stats.put("totalAvailable", totalAvailable);
        stats.put("totalPoints", totalPoints);
        return stats;
    }
}