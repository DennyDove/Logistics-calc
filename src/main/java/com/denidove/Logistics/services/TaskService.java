package com.denidove.Logistics.services;

import com.denidove.Logistics.entities.Task;

import java.util.Optional;

public interface TaskService {

    //public Optional<Task> findById(Long id);
    //public Optional<Task> findByLogin(String login);

    public Long save(Task task);
    //public void saveToDto();

}
