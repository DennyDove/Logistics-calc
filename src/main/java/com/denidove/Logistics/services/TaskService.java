package com.denidove.Logistics.services;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.enums.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskService {

    public Optional<Task> findById(Long id);
    public Optional<Task> findByUserId(Long userId);
    public Optional<Task> findByUserIdAndTaskId(Long userId, Long taskId);

    public List<Task> findAll();
    public List<Task> findAllByStatus(TaskStatus taskStatus);
    public List<Task> findAllByUserId(Long userId);


    public Long save(Task task);
    public void saveToDto(TaskDto task);

    public void updateTaskAdmin(Task task);

}
