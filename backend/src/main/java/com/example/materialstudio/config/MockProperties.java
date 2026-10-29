package com.example.materialstudio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 模拟生成器的可调参数：每步耗时、失败阶段、失败原因、结果地址前缀。
 * 均可通过环境变量覆盖，便于在 README 演示中“调整处理耗时 / 触发指定阶段失败”。
 */
@Component
@ConfigurationProperties(prefix = "mock")
public class MockProperties {

    private long checkDelayMs = 1500;
    private long generateDelayMs = 3000;
    private long finalizeDelayMs = 1500;
    private String failStage = "NONE";
    private String failReason = "模拟生成失败（演示用）";

    public long getCheckDelayMs() {
        return checkDelayMs;
    }

    public void setCheckDelayMs(long checkDelayMs) {
        this.checkDelayMs = checkDelayMs;
    }

    public long getGenerateDelayMs() {
        return generateDelayMs;
    }

    public void setGenerateDelayMs(long generateDelayMs) {
        this.generateDelayMs = generateDelayMs;
    }

    public long getFinalizeDelayMs() {
        return finalizeDelayMs;
    }

    public void setFinalizeDelayMs(long finalizeDelayMs) {
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
