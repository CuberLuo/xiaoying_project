package com.example.materialstudio.mapper;

import com.example.materialstudio.domain.Template;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TemplateMapper {
    List<Template> findAll();

    List<String> findCategories();

    Template findById(@Param("id") String id);
}
