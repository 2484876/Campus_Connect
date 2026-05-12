package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.CommunityPostDTO;
import com.campusconnect.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/communities/posts")
@RequiredArgsConstructor
public class CommunityQAController {

    private final CommunityService communityService;

    @PostMapping("/{postId}/accept-answer/{commentId}")
    public ResponseEntity<CommunityPostDTO> acceptAnswer(@PathVariable Long postId,
                                                         @PathVariable Long commentId,
                                                         @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(communityService.acceptAnswer(postId, commentId, user.getId()));
    }

    @DeleteMapping("/{postId}/accept-answer")
    public ResponseEntity<CommunityPostDTO> unacceptAnswer(@PathVariable Long postId,
                                                           @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(communityService.unacceptAnswer(postId, user.getId()));
    }

    @PostMapping("/{postId}/unmask")
    public ResponseEntity<CommunityPostDTO> unmask(@PathVariable Long postId,
                                                   @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(communityService.unmaskAnonymous(postId, user.getId()));
    }
}