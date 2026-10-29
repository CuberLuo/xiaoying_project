package com.example.materialstudio.web;

import com.example.materialstudio.config.MockProperties;
import com.example.materialstudio.web.request.MockConfigRequest;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 开发/演示接口（R-06）：运行时调整模拟生成器的耗时与失败阶段，无需重启。
 * 例如 POST { "failStage": "GENERATE" } 即可让后续任务在“模拟生成”阶段失败；
 * POST { "generateDelayMs": 8000 } 可拉长处理中观察窗口。
 */
@RestController
@RequestMapping("/api/dev")
public class MockConfigController {

    private final MockProperties mockProperties;

    public MockConfigController(MockProperties mockProperties) {
        this.mockProperties = mockProperties;
    }

    @GetMapping("/mock-config")
    public Map<String, Object> get() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("checkDelayMs", mockProperties.getCheckDelayMs());
        m.put("generateDelayMs", mockProperties.getGenerateDelayMs());
        m.put("finalizeDelayMs", mockProperties.getFinalizeDelayMs());
        m.put("failStage", mockProperties.getFailStage());
        m.put("failReason", mockProperties.getFailReason());
        return m;
    }

    @PostMapping("/mock-config")
    public Map<String, Object> set(@RequestBody MockConfigRequest req) {
        if (req.getCheckDelayMs() != null) {
            mockProperties.setCheckDelayMs(req.getCheckDelayMs());
        }
        if (req.getGenerateDelayMs() != null) {
            mockProperties.setGenerateDelayMs(req.getGenerateDelayMs());
        }
        if (req.getFinalizeDelayMs() != null) {
            mockProperties.setFinalizeDelayMs(req.getFinalizeDelayMs());
        }
        if (req.getFailStage() != null) {
            mockProperties.setFailStage(req.getFailStage());
        }
        if (req.getFailReason() != null) {
            mockProperties.setFailReason(req.getFailReason());
        }
        return get();
    }
}
