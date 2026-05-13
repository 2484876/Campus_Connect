package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.*;
import com.campusconnect.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/career")
@RequiredArgsConstructor
public class CareerController {

    private final ProfileCompletionService completionService;
    private final SkillSearchService skillSearchService;

    @GetMapping("/completion/me")
    public ResponseEntity<ProfileCompletionDTO> getMyCompletion(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(completionService.getCompletion(user.getId()));
    }

    @GetMapping("/completion/user/{userId}")
    public ResponseEntity<ProfileCompletionDTO> getUserCompletion(@PathVariable Long userId) {
        return ResponseEntity.ok(completionService.getCompletion(userId));
    }

    @GetMapping("/skills/search")
    public ResponseEntity<Page<UserDTO>> searchBySkill(@RequestParam String skill,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(skillSearchService.findUsersBySkill(skill, page, size));
    }

    @GetMapping("/skills/autocomplete")
    public ResponseEntity<List<Map<String, Object>>> autocomplete(@RequestParam String q,
                                                                  @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(skillSearchService.autocompleteSkills(q, Math.min(limit, 20)));
    }

    @GetMapping("/skills/trending")
    public ResponseEntity<List<Map<String, Object>>> trending(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(skillSearchService.trendingSkills(Math.min(limit, 30)));
    }
}