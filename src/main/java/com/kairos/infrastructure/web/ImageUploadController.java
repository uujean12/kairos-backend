package com.kairos.infrastructure.web;

import com.kairos.infrastructure.external.CloudinaryImageUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/upload")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ImageUploadController {

    private final CloudinaryImageUploader imageUploader;

    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = imageUploader.upload(file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}