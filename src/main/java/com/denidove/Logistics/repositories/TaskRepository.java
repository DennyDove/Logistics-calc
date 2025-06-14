package com.denidove.Logistics.repositories;

import com.denidove.Logistics.entities.Task;
import com.denidove.Logistics.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository <Task, Long> {

    //public Optional<Task> findById(Long id);
    public Optional<Task> findByUserId(Long userId);
    //toDO Обратить внимание!!! TaskId и Id
    public Optional<Task> findByUserIdAndId(Long userId, Long taskId);

    public List<Task> findAllByUserId(Long id);
    public List<Task> findAllByStatus(TaskStatus taskStatus);
}
