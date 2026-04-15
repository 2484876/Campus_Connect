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

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final FeedService feedService;

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long postId,
                                                 @AuthenticationPrincipal CustomUserDetails user,
                                                 @Valid @RequestBody CreateCommentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedService.addComment(user.getId(), postId, req.getContent()));
    }

    @GetMapping
    public ResponseEntity<Page<CommentDTO>> getComments(@PathVariable Long postId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(feedService.getComments(postId, page, size));
    }
}