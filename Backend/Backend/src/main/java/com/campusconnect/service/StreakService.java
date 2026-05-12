package com.campusconnect.service;

import com.campusconnect.dto.StreakDTO;
import com.campusconnect.entity.UserStreak;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.repository.UserStreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final UserStreakRepository streakRepository;
    private final UserRepository userRepository;
    private final AchievementService achievementService;

    @Transactional
    public StreakDTO checkIn(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        UserStreak streak = streakRepository.findById(userId).orElse(null);
        LocalDate today = LocalDate.now();

        if (streak == null) {
            streak = UserStreak.builder()
                    .userId(userId)
                    .currentStreak(1)
                    .longestStreak(1)
                    .lastActiveDate(today)
                    .totalCheckIns(1)
                    .build();
            streakRepository.save(streak);
            return toDTO(streak, true);
        }

        if (today.equals(streak.getLastActiveDate())) {
            return toDTO(streak, true);
        }

        long daysGap = ChronoUnit.DAYS.between(streak.getLastActiveDate(), today);

        if (daysGap == 1) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            streak.setCurrentStreak(1);
        }

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
        streak.setLastActiveDate(today);
        streak.setTotalCheckIns(streak.getTotalCheckIns() + 1);
        streakRepository.save(streak);

        try {
            if (streak.getCurrentStreak() >= 7) achievementService.award(userId, "WEEK_STREAK");
            if (streak.getCurrentStreak() >= 30) achievementService.award(userId, "MONTH_STREAK");
        } catch (Exception ignored) {}

        return toDTO(streak, true);
    }

    public StreakDTO getMyStreak(Long userId) {
        UserStreak streak = streakRepository.findById(userId).orElse(null);
        if (streak == null) {
            return StreakDTO.builder()
                    .currentStreak(0)
                    .longestStreak(0)
                    .totalCheckIns(0)
                    .checkedInToday(false)
                    .build();
        }
        boolean today = LocalDate.now().equals(streak.getLastActiveDate());
        return toDTO(streak, today);
    }

    private StreakDTO toDTO(UserStreak s, boolean today) {
        return StreakDTO.builder()
                .currentStreak(s.getCurrentStreak())
                .longestStreak(s.getLongestStreak())
                .lastActiveDate(s.getLastActiveDate())
                .totalCheckIns(s.getTotalCheckIns())
                .checkedInToday(today)
                .build();
    }
}