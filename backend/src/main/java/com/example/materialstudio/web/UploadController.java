package com.example.materialstudio.web;

import com.example.materialstudio.domain.Template;
import com.example.materialstudio.service.TemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final TemplateService templateService;

    @Value("${material.upload-dir:./uploads}")
    private String uploadDir;

    public UploadController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String templateId) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        String contentType = file.getContentType();
        long size = file.getSize();

        if (templateId != null && !templateId.isBlank()) {
            Template t = templateService.getById(templateId);
            if (t != null) {
                if (t.getAcceptedMimeTypes() == null || !t.getAcceptedMimeTypes().contains(contentType)) {
                    throw new IllegalArgumentException("文件类型不被模板接受: " + contentType);
                }
                if (size > t.getMaxFileSizeBytes()) {
                    throw new IllegalArgumentException("文件过大，模板上限 " + t.getMaxFileSizeBytes() + " 字节");
                }
            }
        } else {
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("仅支持图片文件");
            }
            if (size > 10L * 1024 * 1024) {
                throw new IllegalArgumentException("文件过大（默认上限 10MB）");
            }
        }

        try {
            String uuid = UUID.randomUUID().toString();
            String orig = file.getOriginalFilename();
            String ext = (orig != null && orig.contains(".")) ? orig.substring(orig.lastIndexOf('.')) : ".bin";
            Path dir = Paths.get(uploadDir, "uploads", uuid).toAbsolutePath();
            Files.createDirectories(dir);
            Path target = dir.resolve("file" + ext);
            file.transferTo(target.toFile());
            String url = "/files/uploads/" + uuid + "/file" + ext;
            return ResponseEntity.ok(Map.of(
                    "url", url,
                    "name", orig == null ? "file" + ext : orig,
                    "size", size,
                    "contentType", contentType));
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage(), e);
        }
    }
}
