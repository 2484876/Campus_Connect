package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.AchievementDTO;
import com.campusconnect.dto.StreakDTO;
import com.campusconnect.dto.UserDTO;
import com.campusconnect.service.AchievementService;
import com.campusconnect.service.StreakService;
import com.campusconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GamificationController {

    private final AchievementService achievementService;
    private final StreakService streakService;
    private final UserService userService;

    @GetMapping("/achievements/me")
    public ResponseEntity<List<AchievementDTO>> myAchievements(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(achievementService.getMyAchievements(user.getId()));
    }

    @GetMapping("/achievements/user/{userId}")
    public ResponseEntity<List<AchievementDTO>> earnedFor(@PathVariable Long userId) {
        return ResponseEntity.ok(achievementService.getEarned(userId));
    }

    @GetMapping("/achievements/stats/{userId}")
    public ResponseEntity<Map<String, Object>> stats(@PathVariable Long userId) {
        return ResponseEntity.ok(achievementService.getStats(userId));
    }

    @PostMapping("/streak/check-in")
    public ResponseEntity<StreakDTO> checkIn(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(streakService.checkIn(user.getId()));
    }

    @GetMapping("/streak/me")
    public ResponseEntity<StreakDTO> myStreak(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(streakService.getMyStreak(user.getId()));
    }

    @GetMapping("/users/celebrants")
    public ResponseEntity<List<UserDTO>> celebrants(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(userService.getTodaysCelebrants(user.getId()));
    }
}