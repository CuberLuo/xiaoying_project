package com.example.materialstudio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 在“结果整理”阶段写一个本地占位结果文件（SVG），使前端能真实预览。
 * 不调用任何真实 AI，仅生成示意图。
 */
@Service
public class MockResultWriter {

    @Value("${material.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${mock.result-base-url:http://localhost:8080/files/results}")
    private String resultBaseUrl;

    public String writePlaceholder(String taskId, String templateTitle) {
        try {
            Path dir = Paths.get(uploadDir, "results").toAbsolutePath();
            Files.createDirectories(dir);
            Path file = dir.resolve(taskId + ".svg");
            String svg = """
                    <svg xmlns="http://www.w3.org/2000/svg" width="480" height="360" viewBox="0 0 480 360">
                      <defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
                        <stop offset="0" stop-color="#0ea5e9"/><stop offset="1" stop-color="#22c55e"/></linearGradient></defs>
                      <rect width="480" height="360" fill="url(#g)"/>
                      <text x="240" y="170" font-family="sans-serif" font-size="26" fill="#fff" text-anchor="middle">%s</text>
                      <text x="240" y="210" font-family="sans-serif" font-size="16" fill="#e0f2fe" text-anchor="middle">模拟结果（演示占位） %s</text>
                    </svg>
                    """.formatted(templateTitle == null ? "结果预览" : templateTitle, taskId);
            Files.writeString(file, svg);
            return resultBaseUrl.replace("/files/results", "") + "/files/results/" + taskId + ".svg";
        } catch (IOException e) {
            throw new RuntimeException("写入模拟结果文件失败: " + e.getMessage(), e);
        }
    }
}
