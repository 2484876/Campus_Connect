package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.*;
import com.campusconnect.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @PostMapping
    public ResponseEntity<StoryDTO> create(@AuthenticationPrincipal CustomUserDetails user,
                                           @RequestBody CreateStoryRequest req) {
        return ResponseEntity.ok(storyService.create(user.getId(), req));
    }

    @GetMapping("/feed")
    public ResponseEntity<List<UserStoriesGroupDTO>> feed(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(storyService.getFeed(user.getId()));
    }

    @PostMapping("/{storyId}/view")
    public ResponseEntity<Void> markViewed(@PathVariable Long storyId,
                                           @AuthenticationPrincipal CustomUserDetails user) {
        storyService.markViewed(storyId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<Void> delete(@PathVariable Long storyId,
                                       @AuthenticationPrincipal CustomUserDetails user) {
        storyService.delete(storyId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{storyId}/viewers")
    public ResponseEntity<List<Map<String, Object>>> viewers(@PathVariable Long storyId,
                                                             @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(storyService.getViewers(storyId, user.getId()));
    }
}