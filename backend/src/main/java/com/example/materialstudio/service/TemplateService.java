package com.example.materialstudio.service;

import com.example.materialstudio.domain.Template;
import com.example.materialstudio.mapper.TemplateMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {

    private final TemplateMapper templateMapper;

    public TemplateService(TemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    public List<Template> listAll() {
        return templateMapper.findAll();
    }

    public List<String> listCategories() {
        return templateMapper.findCategories();
    }

    public Template getById(String id) {
        return templateMapper.findById(id);
    }
}
