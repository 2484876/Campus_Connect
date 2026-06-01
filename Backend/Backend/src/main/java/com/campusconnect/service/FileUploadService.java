package com.campusconnect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.campusconnect.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/webm", "video/quicktime"
    );

    private static final List<String> ALLOWED_AUDIO_TYPES = Arrays.asList(
            "audio/webm", "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/mp4", "audio/x-m4a"
    );

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024;
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;
    private static final long MAX_AUDIO_SIZE = 10 * 1024 * 1024;

    public String uploadImage(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE, "image");
        return upload(file, "campus-connect/images", "image").get("secure_url").toString();
    }

    public String uploadVideo(MultipartFile file) {
        validateFile(file, ALLOWED_VIDEO_TYPES, MAX_VIDEO_SIZE, "video");
        return upload(file, "campus-connect/videos", "video").get("secure_url").toString();
    }

    public String uploadProfilePic(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE, "image");
        return upload(file, "campus-connect/avatars", "image").get("secure_url").toString();
    }

    public Map<String, Object> uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("File is empty");
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 20MB");
        }
        Map result = upload(file, "campus-connect/files", "raw");
        Map<String, Object> out = new HashMap<>();
        out.put("url", result.get("secure_url"));
        out.put("fileName", file.getOriginalFilename());
        out.put("fileSize", file.getSize());
        out.put("contentType", file.getContentType());
        return out;
    }

    public Map<String, Object> uploadVoice(MultipartFile file, Integer durationSeconds) {
        if (file == null || file.isEmpty()) throw new BadRequestException("File is empty");
        if (file.getSize() > MAX_AUDIO_SIZE) {
            throw new BadRequestException("Voice note exceeds 10MB");
        }
        String ct = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_AUDIO_TYPES.contains(ct)) {
            throw new BadRequestException("Invalid audio type: " + ct);
        }
        Map result = upload(file, "campus-connect/voice", "video");
        Map<String, Object> out = new HashMap<>();
        out.put("url", result.get("secure_url"));
        out.put("fileName", file.getOriginalFilename());
        out.put("fileSize", file.getSize());
        out.put("durationSeconds", durationSeconds);
        return out;
    }

    private Map upload(MultipartFile file, String folder, String resourceType) {
        try {
            return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", resourceType
            ));
        } catch (IOException e) {
            throw new BadRequestException("File upload failed: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file, List<String> allowedTypes, long maxSize, String type) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (!allowedTypes.contains(file.getContentType())) {
            throw new BadRequestException("Invalid " + type + " type. Allowed: " + String.join(", ", allowedTypes));
        }
        if (file.getSize() > maxSize) {
            throw new BadRequestException(type + " size exceeds limit of " + (maxSize / 1024 / 1024) + "MB");
        }
    }
}