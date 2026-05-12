package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.PostDTO;
import com.campusconnect.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{postId}")
    public ResponseEntity<Map<String, Boolean>> toggle(@PathVariable Long postId,
                                                       @AuthenticationPrincipal CustomUserDetails user) {
        boolean bookmarked = bookmarkService.toggleBookmark(user.getId(), postId);
        return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
    }

    @GetMapping
    public ResponseEntity<Page<PostDTO>> myBookmarks(@AuthenticationPrincipal CustomUserDetails user,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookmarkService.getMyBookmarks(user.getId(), page, size));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(Map.of("count", bookmarkService.count(user.getId())));
    }
}