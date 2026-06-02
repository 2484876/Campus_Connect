package com.campusconnect.controller;

import com.campusconnect.config.CustomUserDetails;
import com.campusconnect.dto.AdminActionLogDTO;
import com.campusconnect.dto.AdminContentDTO;
import com.campusconnect.dto.AdminStatsDTO;
import com.campusconnect.dto.AdminUserDTO;
import com.campusconnect.dto.ReportDTO;
import com.campusconnect.dto.UpdateUserRoleRequest;
import com.campusconnect.dto.UpdateUserStatusRequest;
import com.campusconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> stats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDTO>> users(@RequestParam(required = false) String q,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) String role,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listUsers(q, status, role, page, size));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDTO> user(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUser(id));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<Void> setStatus(@PathVariable Long id,
                                          @AuthenticationPrincipal CustomUserDetails admin,
                                          @RequestBody UpdateUserStatusRequest req) {
        adminService.setUserStatus(admin.getId(), id, req.isActive(), req.getReason());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Void> setRole(@PathVariable Long id,
                                        @AuthenticationPrincipal CustomUserDetails admin,
                                        @RequestBody UpdateUserRoleRequest req) {
        adminService.setUserRole(admin.getId(), id, req.getRole());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetails admin,
                                           @RequestParam(defaultValue = "SOFT") String mode) {
        adminService.deleteUser(admin.getId(), id, mode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports")
    public ResponseEntity<Page<ReportDTO>> reports(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listReports(page, size));
    }

    @PutMapping("/reports/{id}")
    public ResponseEntity<Void> resolveReport(@PathVariable Long id,
                                              @AuthenticationPrincipal CustomUserDetails admin,
                                              @RequestParam String status) {
        adminService.resolveReport(admin.getId(), id, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/content/posts")
    public ResponseEntity<Page<AdminContentDTO>> posts(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listPosts(page, size));
    }

    @DeleteMapping("/content/{type}/{id}")
    public ResponseEntity<Void> removeContent(@PathVariable String type,
                                              @PathVariable Long id,
                                              @AuthenticationPrincipal CustomUserDetails admin) {
        adminService.removeContent(admin.getId(), type, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/logs")
    public ResponseEntity<Page<AdminActionLogDTO>> logs(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(adminService.listLogs(page, size));
    }
}