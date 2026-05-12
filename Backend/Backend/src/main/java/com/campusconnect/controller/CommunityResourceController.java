package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.CommunityResourceDTO;
import com.campusconnect.dto.CreateResourceRequest;
import com.campusconnect.service.CommunityResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/communities/{communityId}/resources")
@RequiredArgsConstructor
public class CommunityResourceController {

    private final CommunityResourceService resourceService;

    @GetMapping
    public ResponseEntity<Page<CommunityResourceDTO>> list(@PathVariable Long communityId,
                                                           @RequestParam(required = false) String q,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(resourceService.list(communityId, q, page, size));
    }

    @PostMapping
    public ResponseEntity<CommunityResourceDTO> add(@PathVariable Long communityId,
                                                    @AuthenticationPrincipal CustomUserDetails user,
                                                    @Valid @RequestBody CreateResourceRequest req) {
        return ResponseEntity.ok(resourceService.add(communityId, user.getId(), req));
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> delete(@PathVariable Long communityId,
                                       @PathVariable Long resourceId,
                                       @AuthenticationPrincipal CustomUserDetails user) {
        resourceService.delete(resourceId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{resourceId}/click")
    public ResponseEntity<CommunityResourceDTO> click(@PathVariable Long communityId,
                                                      @PathVariable Long resourceId) {
        return ResponseEntity.ok(resourceService.trackClick(resourceId));
    }
}