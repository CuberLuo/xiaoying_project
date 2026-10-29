package com.example.materialstudio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 将本地落盘的素材与结果文件通过 HTTP 暴露：
 *   /files/uploads/** -> {upload-dir}/uploads/**
 *   /files/results/** -> {upload-dir}/results/**
 * 这样前端可以直接预览上传图与生成结果（均为本地可访问文件，不依赖外部服务）。
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${material.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path base = Paths.get(uploadDir).toAbsolutePath();
        String location = base.toString().replace("\\", "/") + "/";
        registry.addResourceHandler("/files/uploads/**")
                .addResourceLocations("file:" + location + "uploads/");
        registry.addResourceHandler("/files/results/**")
                .addResourceLocations("file:" + location + "results/");
    }
}
