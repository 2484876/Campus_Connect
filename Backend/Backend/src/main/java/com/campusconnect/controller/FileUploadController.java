package com.campusconnect.controller;

import com.campusconnect.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/video")
    public ResponseEntity<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadVideo(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadProfilePic(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}