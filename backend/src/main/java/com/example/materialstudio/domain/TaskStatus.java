package com.example.materialstudio.domain;

/**
 * 任务整体状态。至少表达：等待处理、处理中、成功、失败。
 * 终态为 SUCCESS / FAILED，进入终态后不允许再回退为 PROCESSING。
 */
public enum TaskStatus {
    PENDING,    // 等待处理（已受理，尚未开始后台步骤）
    PROCESSING, // 处理中
    SUCCESS,    // 成功
    FAILED;     // 失败

    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED;
    }
}
