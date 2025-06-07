package com.denidove.Logistics.services;

import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.repositories.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    //public Optional<Task> findById(Long id);
    //public Optional<Task> findByLogin(String login);

    public Long save(Task task) {

        return taskRepository.save(task).getId();
    }

    //public void saveToDto();

}
