package com.example.materialstudio.web;

import com.example.materialstudio.domain.Task;
import com.example.materialstudio.service.TaskService;
import com.example.materialstudio.web.request.SubmitTaskRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** 提交制作：受理即返回任务（含 ID），后台异步执行，不等待结束。 */
    @PostMapping("/templates/{templateId}/tasks")
    public Task submit(@PathVariable String templateId,
                       @RequestHeader(value = "X-User-Id", required = false, defaultValue = "demo-user") String userId,
                       @RequestBody SubmitTaskRequest body) {
        return taskService.submit(userId, templateId, body.getFileUrls());
    }

    /** 个人历史列表（只读查询）。 */
    @GetMapping("/tasks")
    public List<Task> history(@RequestHeader(value = "X-User-Id", required = false, defaultValue = "demo-user") String userId) {
        return taskService.listByUser(userId);
    }

    /** 单任务查询（只读，绝不驱动任务向下一步推进）。 */
    @GetMapping("/tasks/{id}")
    public Task get(@PathVariable String id) {
        Task t = taskService.getById(id);
        if (t == null) {
            throw new IllegalArgumentException("任务不存在: " + id);
        }
        return t;
    }
}
