package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.*;
import com.campusconnect.service.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
/**
 * FeedController handles all Social Feed and Post-related operations.
 *   APIs include:
 * - Fetching personalized and public user feeds
 * - Retrieving posts by specific users
 * - Creating and deleting posts
 * - Fetching single post details
 * - Toggling likes on posts
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeedController {
     
    private final FeedService feedService;
    
    @GetMapping("/feed")
    public ResponseEntity<Page<PostDTO>> getFeed(@AuthenticationPrincipal CustomUserDetails user,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(feedService.getFeed(user.getId(), page, size));
    }

    @GetMapping("/feed/public")
    public ResponseEntity<Page<PostDTO>> getPublicFeed(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(feedService.getPublicFeed(page, size));
    }

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<Page<PostDTO>> getUserPosts(@PathVariable Long userId,
                                                      @AuthenticationPrincipal CustomUserDetails user,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(feedService.getUserPosts(userId, user.getId(), page, size));
    }

    @PostMapping("/posts")
    public ResponseEntity<PostDTO> createPost(@AuthenticationPrincipal CustomUserDetails user,
                                              @Valid @RequestBody CreatePostRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedService.createPost(user.getId(), req));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long postId,
                                           @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(feedService.getPostById(postId, user.getId()));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId,
                                           @AuthenticationPrincipal CustomUserDetails user) {
        feedService.deletePost(user.getId(), postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long postId,
                                                          @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(feedService.toggleLike(user.getId(), postId));
    }
}