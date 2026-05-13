package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.HashtagDTO;
import com.campusconnect.dto.PostDTO;
import com.campusconnect.service.HashtagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hashtags")
@RequiredArgsConstructor
public class HashtagController {

    private final HashtagService hashtagService;

    @GetMapping("/trending")
    public ResponseEntity<List<HashtagDTO>> trending(@RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(hashtagService.trending(limit));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HashtagDTO>> search(@RequestParam String q,
                                                   @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(hashtagService.search(q, limit));
    }

    @GetMapping("/{tag}/posts")
    public ResponseEntity<List<PostDTO>> postsByTag(@PathVariable String tag,
                                                    @AuthenticationPrincipal CustomUserDetails user) {
        Long uid = user != null ? user.getId() : null;
        return ResponseEntity.ok(hashtagService.postsByTag(tag, uid));
    }
}