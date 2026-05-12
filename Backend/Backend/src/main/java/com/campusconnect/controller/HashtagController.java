package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.HashtagDTO;
import com.campusconnect.dto.PostDTO;
import com.campusconnect.repository.PostRepository;
import com.campusconnect.service.FeedService;
import com.campusconnect.service.HashtagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/hashtags")
@RequiredArgsConstructor
public class HashtagController {

    private final HashtagService hashtagService;
    private final PostRepository postRepository;
    private final FeedService feedService;

    @GetMapping("/trending")
    public ResponseEntity<List<HashtagDTO>> trending(@RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(hashtagService.getTrending(limit));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HashtagDTO>> search(@RequestParam String q,
                                                   @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(hashtagService.search(q, limit));
    }

    @GetMapping("/{tag}/posts")
    public ResponseEntity<List<PostDTO>> postsForTag(@PathVariable String tag,
                                                     @AuthenticationPrincipal CustomUserDetails user) {
        List<Long> ids = hashtagService.findPostIdsByTag(tag);
        List<PostDTO> posts = new ArrayList<>();
        for (Long id : ids) {
            postRepository.findById(id).ifPresent(p -> {
                if (p.isActive()) posts.add(feedService.mapToDTO(p, user.getId()));
            });
        }
        return ResponseEntity.ok(posts);
    }
}