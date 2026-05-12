package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.SearchResultDTO;
import com.campusconnect.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResultDTO> search(@RequestParam String q,
                                                  @AuthenticationPrincipal CustomUserDetails user) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(SearchResultDTO.builder().build());
        }
        return ResponseEntity.ok(searchService.universalSearch(q.trim(), user.getId()));
    }
}