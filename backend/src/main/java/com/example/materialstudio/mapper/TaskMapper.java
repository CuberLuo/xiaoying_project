package com.example.materialstudio.mapper;

import com.example.materialstudio.domain.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {
    void insert(Task task);

    Task findById(@Param("id") String id);

    List<Task> findByUserId(@Param("userId") String userId);

    void update(Task task);
}
