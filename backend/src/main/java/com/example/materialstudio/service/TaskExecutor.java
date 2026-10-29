package com.example.materialstudio.service;

import com.example.materialstudio.config.MockProperties;
import com.example.materialstudio.domain.Task;
import com.example.materialstudio.domain.TaskStage;
import com.example.materialstudio.domain.TaskStatus;
import com.example.materialstudio.mapper.TaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 多步骤异步任务引擎（本题核心）。
 *
 * 状态机：素材检查(CHECK) -> 模拟生成(GENERATE) -> 结果整理(FINALIZE) -> 完成(DONE)
 * 规则：
 *  - 每一步有独立、可观察的执行边界（各自写库 + 模拟耗时）；
 *  - 某一步失败立即停止后续步骤，并保存失败阶段与原因；
 *  - 进入终态(SUCCESS/FAILED)后不再回退为处理中（终态守卫）；
 *  - 仅本执行器推进状态，查询接口只读，绝不驱动任务前进。
 *
 * processTask 为同步、可单测的核心逻辑；execute 仅负责异步调度（经 @Async 代理）。
 */
@Service
public class TaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);

    private final TaskMapper taskMapper;
    private final MockProperties mockProperties;
    private final MockResultWriter resultWriter;

    public TaskExecutor(TaskMapper taskMapper, MockProperties mockProperties, MockResultWriter resultWriter) {
        this.taskMapper = taskMapper;
        this.mockProperties = mockProperties;
        this.resultWriter = resultWriter;
    }

    @Async("taskWorkerExecutor")
    public void execute(String taskId) {
        processTask(taskId);
    }

    public void processTask(String taskId) {
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            log.warn("任务不存在，无法执行: {}", taskId);
            return;
        }
        // 终态守卫：已完成/已失败的任务不重复执行
        if (TaskStatus.valueOf(task.getStatus()).isTerminal()) {
            return;
        }

        try {
            // 步骤一：素材检查
            transition(task, TaskStatus.PROCESSING, TaskStage.CHECK, "素材检查中");
            sleep(mockProperties.getCheckDelayMs());
            if (shouldFailAt("CHECK")) {
                fail(task, TaskStage.CHECK, mockProperties.getFailReason());
                return;
            }

            // 步骤二：模拟生成
            transition(task, TaskStatus.PROCESSING, TaskStage.GENERATE, "模拟生成中");
            sleep(mockProperties.getGenerateDelayMs());
            if (shouldFailAt("GENERATE")) {
                fail(task, TaskStage.GENERATE, mockProperties.getFailReason());
                return;
            }

            // 步骤三：结果整理
            transition(task, TaskStatus.PROCESSING, TaskStage.FINALIZE, "结果整理中");
            sleep(mockProperties.getFinalizeDelayMs());
            if (shouldFailAt("FINALIZE")) {
                fail(task, TaskStage.FINALIZE, mockProperties.getFailReason());
                return;
            }

            // 成功：写入占位结果并置为完成
            String resultUrl = resultWriter.writePlaceholder(taskId, task.getTemplateTitle());
            task.setStatus(TaskStatus.SUCCESS.name());
            task.setStage(TaskStage.DONE.name());
            task.setStageDetail("完成");
            task.setResultUrl(resultUrl);
            task.setUpdatedAt(java.time.LocalDateTime.now());
            taskMapper.update(task);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail(task, TaskStage.valueOf(task.getStage()), "任务被中断");
        } catch (Exception e) {
            // 任何意外异常都标记失败，避免任务永远卡在处理中
            fail(task, TaskStage.valueOf(task.getStage()), "执行异常：" + e.getMessage());
        }
    }

    private void transition(Task task, TaskStatus status, TaskStage stage, String detail) {
        task.setStatus(status.name());
        task.setStage(stage.name());
        task.setStageDetail(detail);
        task.setUpdatedAt(java.time.LocalDateTime.now());
        taskMapper.update(task);
    }

    private void fail(Task task, TaskStage stage, String reason) {
        task.setStatus(TaskStatus.FAILED.name());
        task.setStage(stage.name());
        task.setStageDetail("失败：" + reason);
        task.setFailReason(reason);
        task.setUpdatedAt(java.time.LocalDateTime.now());
        taskMapper.update(task);
        log.info("任务 {} 在阶段 {} 失败：{}", task.getId(), stage, reason);
    }

    private boolean shouldFailAt(String stage) {
        return stage.equalsIgnoreCase(mockProperties.getFailStage());
    }

    private void sleep(long ms) throws InterruptedException {
        if (ms > 0) {
            Thread.sleep(ms);
        }
    }
}
