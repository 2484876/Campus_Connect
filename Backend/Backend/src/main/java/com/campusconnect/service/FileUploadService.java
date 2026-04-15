package com.campusconnect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.campusconnect.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Arrays;
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

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024;

    public String uploadImage(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE, "image");
        return upload(file, "campus-connect/images", "image");
    }

    public String uploadVideo(MultipartFile file) {
        validateFile(file, ALLOWED_VIDEO_TYPES, MAX_VIDEO_SIZE, "video");
        return upload(file, "campus-connect/videos", "video");
    }

    public String uploadProfilePic(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE, "image");
        return upload(file, "campus-connect/avatars", "image");
    }

    private String upload(MultipartFile file, String folder, String resourceType) {
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", resourceType
            ));
            return (String) result.get("secure_url");
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