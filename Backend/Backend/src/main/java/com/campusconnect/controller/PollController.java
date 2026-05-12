package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.CreatePollRequest;
import com.campusconnect.dto.PollDTO;
import com.campusconnect.service.PollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PostMapping("/posts/{postId}")
    public ResponseEntity<PollDTO> create(@PathVariable Long postId,
                                          @AuthenticationPrincipal CustomUserDetails user,
                                          @Valid @RequestBody CreatePollRequest req) {
        return ResponseEntity.ok(pollService.createPoll(user.getId(), postId, req));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PollDTO> getForPost(@PathVariable Long postId,
                                              @AuthenticationPrincipal CustomUserDetails user) {
        PollDTO poll = pollService.getPollForPost(postId, user.getId());
        return ResponseEntity.ok(poll);
    }

    @PostMapping("/{pollId}/vote/{optionId}")
    public ResponseEntity<PollDTO> vote(@PathVariable Long pollId,
                                        @PathVariable Long optionId,
                                        @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(pollService.vote(pollId, optionId, user.getId()));
    }
}