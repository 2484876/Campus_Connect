package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.*;
import com.campusconnect.service.FileUploadService;
import com.campusconnect.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileUploadService fileUploadService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getProfile(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(userService.getProfile(user.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(@AuthenticationPrincipal CustomUserDetails user,
                                                 @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userService.updateProfile(user.getId(), req));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<UserDTO> uploadAvatar(@AuthenticationPrincipal CustomUserDetails user,
                                                @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadProfilePic(file);
        userService.updateProfilePicUrl(user.getId(), url);
        return ResponseEntity.ok(userService.getProfile(user.getId()));
    }

    @PostMapping("/me/banner")
    public ResponseEntity<UserDTO> uploadBanner(@AuthenticationPrincipal CustomUserDetails user,
                                                @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadImage(file);
        userService.updateBannerUrl(user.getId(), url);
        return ResponseEntity.ok(userService.getProfile(user.getId()));
    }

    @PostMapping("/me/experience")
    public ResponseEntity<ExperienceDTO> addExperience(@AuthenticationPrincipal CustomUserDetails user,
                                                       @Valid @RequestBody CreateExperienceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addExperience(user.getId(), req));
    }

    @DeleteMapping("/me/experience/{expId}")
    public ResponseEntity<Void> deleteExperience(@AuthenticationPrincipal CustomUserDetails user,
                                                 @PathVariable Long expId) {
        userService.deleteExperience(user.getId(), expId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id,
                                               @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(userService.getUserById(id, user.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserDTO>> searchUsers(@RequestParam String q,
                                                     @AuthenticationPrincipal CustomUserDetails user,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.searchUsers(q, user.getId(), page, size));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<Page<UserDTO>> getSuggestions(@AuthenticationPrincipal CustomUserDetails user,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getSuggestions(user.getId(), page, size));
    }

    @GetMapping("/by-role")
    public ResponseEntity<Page<UserDTO>> getUsersByRole(@RequestParam String role,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getUsersByRole(role, page, size));
    }
}