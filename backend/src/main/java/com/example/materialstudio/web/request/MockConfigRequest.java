package com.example.materialstudio.web.request;

public class MockConfigRequest {
    private Long checkDelayMs;
    private Long generateDelayMs;
    private Long finalizeDelayMs;
    private String failStage;
    private String failReason;

    public Long getCheckDelayMs() {
        return checkDelayMs;
    }

    public void setCheckDelayMs(Long checkDelayMs) {
        this.checkDelayMs = checkDelayMs;
    }

    public Long getGenerateDelayMs() {
        return generateDelayMs;
    }

    public void setGenerateDelayMs(Long generateDelayMs) {
        this.generateDelayMs = generateDelayMs;
    }

    public Long getFinalizeDelayMs() {
        return finalizeDelayMs;
    }

    public void setFinalizeDelayMs(Long finalizeDelayMs) {
        this.finalizeDelayMs = finalizeDelayMs;
    }

    public String getFailStage() {
        return failStage;
    }

    public void setFailStage(String failStage) {
        this.failStage = failStage;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}
