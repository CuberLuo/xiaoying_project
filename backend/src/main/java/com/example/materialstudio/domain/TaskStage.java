package com.example.materialstudio.domain;

/**
 * 后台当前执行阶段。每一步有独立可观察的执行边界。
 * 顺序：CHECK -> GENERATE -> FINALIZE -> DONE
 */
public enum TaskStage {
    NONE,       // 尚未开始
    CHECK,      // 素材检查
    GENERATE,   // 模拟生成
    FINALIZE,   // 结果整理
    DONE;       // 全部完成（与 SUCCESS 同时出现）
}
