package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.*;
import com.campusconnect.service.CommunityService;
import com.campusconnect.service.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<CommunityDTO> create(@AuthenticationPrincipal CustomUserDetails user,
                                               @Valid @RequestBody CreateCommunityRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(communityService.createCommunity(user.getId(), req));
    }

    @GetMapping
    public ResponseEntity<Page<CommunityDTO>> getAll(@AuthenticationPrincipal CustomUserDetails user,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(communityService.getAllCommunities(user.getId(), page, size));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<CommunityDTO>> getMine(@AuthenticationPrincipal CustomUserDetails user,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(communityService.getMyCommunities(user.getId(), page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CommunityDTO>> search(@RequestParam String q,
                                                     @AuthenticationPrincipal CustomUserDetails user,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(communityService.searchCommunities(q, user.getId(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityDTO> getById(@PathVariable Long id,
                                                @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(communityService.getCommunityById(id, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommunityDTO> update(@PathVariable Long id,
                                               @AuthenticationPrincipal CustomUserDetails user,
                                               @RequestBody CreateCommunityRequest req) {
        return ResponseEntity.ok(communityService.updateCommunity(id, user.getId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails user) {
        communityService.deleteCommunity(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/icon")
    public ResponseEntity<CommunityDTO> uploadIcon(@PathVariable Long id,
                                                   @AuthenticationPrincipal CustomUserDetails user,
                                                   @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadImage(file);
        return ResponseEntity.ok(communityService.updateIcon(id, user.getId(), url));
    }

    @PostMapping("/{id}/banner")
    public ResponseEntity<CommunityDTO> uploadBanner(@PathVariable Long id,
                                                     @AuthenticationPrincipal CustomUserDetails user,
                                                     @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadImage(file);
        return ResponseEntity.ok(communityService.updateBanner(id, user.getId(), url));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<CommunityDTO> join(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(communityService.joinCommunity(id, user.getId()));
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leave(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetails user) {
        communityService.leaveCommunity(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<Page<UserDTO>> getMembers(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(communityService.getMembers(id, page, size));
    }

    @PutMapping("/{id}/members/{userId}/role")
    public ResponseEntity<Map<String, String>> updateMemberRole(@PathVariable Long id,
                                                                @PathVariable Long userId,
                                                                @AuthenticationPrincipal CustomUserDetails user,
                                                                @RequestBody Map<String, String> body) {
        communityService.updateMemberRole(id, user.getId(), userId, body.get("role"));
        return ResponseEntity.ok(Map.of("message", "Role updated"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id,
                                             @PathVariable Long userId,
                                             @AuthenticationPrincipal CustomUserDetails user) {
        communityService.removeMember(id, user.getId(), userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/posts")
    public ResponseEntity<CommunityPostDTO> createPost(@PathVariable Long id,
                                                       @AuthenticationPrincipal CustomUserDetails user,
                                                       @Valid @RequestBody CreateCommunityPostRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(communityService.createPost(id, user.getId(), req));
    }

    @GetMapping("/{id}/posts")
    public ResponseEntity<Page<CommunityPostDTO>> getPosts(@PathVariable Long id,
                                                           @AuthenticationPrincipal CustomUserDetails user,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(communityService.getCommunityPosts(id, user.getId(), page, size));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId,
                                           @AuthenticationPrincipal CustomUserDetails user) {
        communityService.deletePost(postId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<CommunityPostDTO>> getFeed(@AuthenticationPrincipal CustomUserDetails user,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(communityService.getCommunityFeed(user.getId(), page, size));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<CommunityPostDTO> getPost(@PathVariable Long postId,
                                                    @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(communityService.getPostById(postId, user.getId()));
    }

    @PostMapping("/posts/{postId}/vote")
    public ResponseEntity<Map<String, Object>> votePost(@PathVariable Long postId,
                                                        @AuthenticationPrincipal CustomUserDetails user,
                                                        @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(communityService.votePost(postId, user.getId(), body.get("value")));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommunityCommentDTO> addComment(@PathVariable Long postId,
                                                          @AuthenticationPrincipal CustomUserDetails user,
                                                          @Valid @RequestBody CreateCommunityCommentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(communityService.addComment(postId, user.getId(), req));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommunityCommentDTO>> getComments(@PathVariable Long postId,
                                                                 @AuthenticationPrincipal CustomUserDetails user,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(communityService.getComments(postId, user.getId(), page, size));
    }

    @PostMapping("/comments/{commentId}/vote")
    public ResponseEntity<Map<String, Object>> voteComment(@PathVariable Long commentId,
                                                           @AuthenticationPrincipal CustomUserDetails user,
                                                           @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(communityService.voteComment(commentId, user.getId(), body.get("value")));
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<Map<String, String>> inviteUser(@PathVariable Long id,
                                                          @AuthenticationPrincipal CustomUserDetails user,
                                                          @RequestBody Map<String, Long> body) {
        communityService.inviteUser(id, user.getId(), body.get("receiverId"));
        return ResponseEntity.ok(Map.of("message", "Invitation sent"));
    }
}