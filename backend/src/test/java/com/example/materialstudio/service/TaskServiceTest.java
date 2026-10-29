package com.example.materialstudio.service;

import com.example.materialstudio.domain.Task;
import com.example.materialstudio.domain.TaskStatus;
import com.example.materialstudio.domain.Template;
import com.example.materialstudio.mapper.TaskMapper;
import com.example.materialstudio.mapper.TemplateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 任务受理与查询的契约验证：
 *  - submit 在参数校验通过后建记录(PENDING)并返回 ID，触发后台执行（不等待）；
 *  - 参数（素材数量/模板存在）不合法时抛错；
 *  - getById 是只读查询：即便底层查询异常，也不会把任务标记为失败或写入任何状态。
 */
class TaskServiceTest {

    private TaskMapper taskMapper;
    private TemplateMapper templateMapper;
    private TaskExecutor taskExecutor;
    private TaskService service;

    @BeforeEach
    void setUp() {
        taskMapper = mock(TaskMapper.class);
        templateMapper = mock(TemplateMapper.class);
        taskExecutor = mock(TaskExecutor.class);
        service = new TaskService(taskMapper, templateMapper, taskExecutor);
    }

    private Template template(int required) {
        Template t = new Template();
        t.setId("t1");
        t.setTitle("测试模板");
        t.setRequiredImageCount(required);
        t.setAcceptedMimeTypes("[\"image/jpeg\",\"image/png\"]");
        t.setMaxFileSizeBytes(5242880);
        return t;
    }

    @Test
    void submit_creates_pending_task_and_triggers_executor() {
        when(templateMapper.findById("t1")).thenReturn(template(2));

        Task task = service.submit("u1", "t1", List.of("/files/uploads/a", "/files/uploads/b"));

        assertNotNull(task.getId());
        assertEquals(TaskStatus.PENDING.name(), task.getStatus());
        verify(taskMapper).insert(any(Task.class));
        verify(taskExecutor).execute(eq(task.getId()));
    }

    @Test
    void submit_rejects_wrong_image_count() {
        when(templateMapper.findById("t1")).thenReturn(template(2));
        assertThrows(IllegalArgumentException.class,
                () -> service.submit("u1", "t1", List.of("/files/uploads/a")));
        verify(taskMapper, never()).insert(any());
    }

    @Test
    void getById_is_read_only_and_never_mutates_on_query_error() {
        when(taskMapper.findById("task-x")).thenThrow(new RuntimeException("db unavailable"));

        assertThrows(RuntimeException.class, () -> service.getById("task-x"));
        // 查询异常绝不应触发任何状态写入
        verify(taskMapper, never()).update(any());
    }
}
