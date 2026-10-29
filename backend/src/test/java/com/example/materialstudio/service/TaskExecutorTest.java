package com.example.materialstudio.service;

import com.example.materialstudio.config.MockProperties;
import com.example.materialstudio.domain.Task;
import com.example.materialstudio.domain.TaskStatus;
import com.example.materialstudio.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 核心：多步骤异步任务引擎的行为验证（同步调用 processTask，绕过 @Async）。
 * 覆盖：(1) 成功时三步顺序推进并落库结果；(2) 指定阶段失败后停止后续步骤；
 *       (3) 终态守卫——已完成的任务不会被重复执行。
 */
class TaskExecutorTest {

    private TaskMapper mapper;
    private MockProperties props;
    private TaskExecutor executor;

    @BeforeEach
    void setUp() {
        mapper = mock(TaskMapper.class);
        props = new MockProperties();
        props.setCheckDelayMs(0);
        props.setGenerateDelayMs(0);
        props.setFinalizeDelayMs(0);
        props.setFailStage("NONE");
        props.setFailReason("模拟失败");
        executor = new TaskExecutor(mapper, props, mock(com.example.materialstudio.service.MockResultWriter.class));
    }

    private Task pendingTask() {
        Task t = new Task();
        t.setId("task-1");
        t.setUserId("u1");
        t.setTemplateId("t1");
        t.setTemplateTitle("测试模板");
        t.setRequiredImageCount(2);
        t.setStatus(TaskStatus.PENDING.name());
        t.setStage("NONE");
        return t;
    }

    @Test
    void success_runs_all_three_steps_and_records_result() {
        when(mapper.findById("task-1")).thenReturn(pendingTask());
        com.example.materialstudio.service.MockResultWriter writer = mock(com.example.materialstudio.service.MockResultWriter.class);
        when(writer.writePlaceholder(any(), any())).thenReturn("/files/results/task-1.svg");
        TaskExecutor exec = new TaskExecutor(mapper, props, writer);

        exec.processTask("task-1");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(mapper, atLeast(4)).update(captor.capture());
        List<Task> updates = captor.getAllValues();
        // 末次更新应为成功态
        Task last = updates.get(updates.size() - 1);
        assertEquals(TaskStatus.SUCCESS.name(), last.getStatus());
        assertEquals("DONE", last.getStage());
        assertTrue(last.getResultUrl() != null && last.getResultUrl().contains("/files/results/"));
    }

    @Test
    void failure_at_generate_stops_and_records_stage_and_reason() {
        props.setFailStage("GENERATE");
        when(mapper.findById("task-1")).thenReturn(pendingTask());

        executor.processTask("task-1");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(mapper, atLeast(1)).update(captor.capture());
        Task last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(TaskStatus.FAILED.name(), last.getStatus());
        assertEquals("GENERATE", last.getStage());
        assertNotNull(last.getFailReason());
        assertNull(last.getResultUrl(), "失败时不应产生结果地址");
    }

    @Test
    void terminal_guard_prevents_rerun_of_finished_task() {
        Task done = pendingTask();
        done.setStatus(TaskStatus.SUCCESS.name());
        done.setStage("DONE");
        when(mapper.findById("task-1")).thenReturn(done);

        executor.processTask("task-1");

        // 已终态的任务不应再触发任何更新
        verify(mapper, never()).update(any());
    }
}
