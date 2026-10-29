package com.example.materialstudio.web;

import com.example.materialstudio.domain.Template;
import com.example.materialstudio.service.TemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public List<Template> list(@RequestParam(required = false) String category) {
        List<Template> all = templateService.listAll();
        if (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category)) {
            return all;
        }
        return all.stream().filter(t -> category.equals(t.getCategory())).toList();
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return templateService.listCategories();
    }

    @GetMapping("/{id}")
    public Template get(@PathVariable String id) {
        Template t = templateService.getById(id);
        if (t == null) {
            throw new IllegalArgumentException("模板不存在: " + id);
        }
        return t;
    }
}
