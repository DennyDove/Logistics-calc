package com.denidove.Logistics.services.impl;

import com.denidove.Logistics.dto.TaskDto;
import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.enums.TaskStatus;
import com.denidove.Logistics.repositories.TaskRepository;
import com.denidove.Logistics.services.TaskService;
import com.denidove.Logistics.services.UserSessionService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserSessionService userSessionService;

    public TaskServiceImpl(TaskRepository taskRepository, UserSessionService userSessionService) {
        this.taskRepository = taskRepository;
        this.userSessionService = userSessionService;
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public Optional<Task> findByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    public Optional<Task> findByUserIdAndTaskId(Long userId, Long taskId) {
        return taskRepository.findByUserIdAndId(userId, taskId);
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public List<Task> findAllByStatus(TaskStatus taskStatus) {
        return taskRepository.findAllByStatus(taskStatus);
    }

    public List<Task> findAllByUserId(Long userId) {
        return taskRepository.findAllByUserId(userId);
    }

    public Long save(Task task) {
        task.setTimeDate(new Timestamp(System.currentTimeMillis()));
        task.setStatus(TaskStatus.InWork);
        return taskRepository.save(task).getId();
    }

    public void saveToDto(String company, TaskDto taskDto) {
        userSessionService.getTaskDto().put(company, taskDto);
    }


    public void updateTaskAdmin(Task task) {
        taskRepository.save(task);
    }

}
