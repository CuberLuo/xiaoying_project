package com.example.materialstudio.domain;

import java.time.LocalDateTime;

public class Task {
    private String id;
    private String userId;
    private String templateId;
    private String templateTitle;
    private int requiredImageCount;
    private String status;       // TaskStatus.name()
    private String stage;        // TaskStage.name()
    private String stageDetail;  // 当前阶段的中文说明，前端直接展示
    private String resultUrl;    // 成功后结果预览地址
    private String failReason;   // 失败后原因
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateTitle() {
        return templateTitle;
    }

    public void setTemplateTitle(String templateTitle) {
        this.templateTitle = templateTitle;
    }

    public int getRequiredImageCount() {
        return requiredImageCount;
    }

    public void setRequiredImageCount(int requiredImageCount) {
        this.requiredImageCount = requiredImageCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStageDetail() {
        return stageDetail;
    }

    public void setStageDetail(String stageDetail) {
        this.stageDetail = stageDetail;
    }

    public String getResultUrl() {
        return resultUrl;
    }

    public void setResultUrl(String resultUrl) {
        this.resultUrl = resultUrl;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
