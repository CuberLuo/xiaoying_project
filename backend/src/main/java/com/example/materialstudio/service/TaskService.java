package com.example.materialstudio.service;

import com.example.materialstudio.domain.Task;
import com.example.materialstudio.domain.TaskStage;
import com.example.materialstudio.domain.TaskStatus;
import com.example.materialstudio.domain.Template;
import com.example.materialstudio.mapper.TaskMapper;
import com.example.materialstudio.mapper.TemplateMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskMapper taskMapper;
    private final TemplateMapper templateMapper;
    private final TaskExecutor taskExecutor;

    public TaskService(TaskMapper taskMapper, TemplateMapper templateMapper, TaskExecutor taskExecutor) {
        this.taskMapper = taskMapper;
        this.templateMapper = templateMapper;
        this.taskExecutor = taskExecutor;
    }

    /**
     * 受理提交：校验参数与素材 -> 建任务记录(PENDING) -> 返回任务 ID，不等待后台执行。
     * 后台步骤由 TaskExecutor 异步继续，关闭页面不中断。
     */
    public Task submit(String userId, String templateId, List<String> fileUrls) {
        Template template = templateMapper.findById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在: " + templateId);
        }
        int required = template.getRequiredImageCount();
        if (fileUrls == null || fileUrls.size() != required) {
            throw new IllegalArgumentException(
                    "素材数量不正确，模板“" + template.getTitle() + "”需要 " + required + " 张图片");
        }
        // 服务端可用校验：只接受本服务落盘、以 /files/uploads/ 开头的文件
        for (String url : fileUrls) {
            if (url == null || !url.startsWith("/files/uploads/")) {
                throw new IllegalArgumentException("素材地址无效，必须使用本服务上传返回的文件: " + url);
            }
        }

        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setUserId(userId);
        task.setTemplateId(templateId);
        task.setTemplateTitle(template.getTitle());
        task.setRequiredImageCount(required);
        task.setStatus(TaskStatus.PENDING.name());
        task.setStage(TaskStage.NONE.name());
        task.setStageDetail("已受理，等待处理");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.insert(task);

        // 受理后立即返回任务 ID，后台步骤异步执行
        taskExecutor.execute(task.getId());
        return task;
    }

    /** 只读查询：绝不驱动任务向下一步推进。 */
    public Task getById(String id) {
        return taskMapper.findById(id);
    }

    /** 个人历史：按用户过滤，最新在前。 */
    public List<Task> listByUser(String userId) {
        return taskMapper.findByUserId(userId);
    }
}
