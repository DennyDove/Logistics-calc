package com.denidove.Logistics.repositories;

import com.denidove.Logistics.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository <Task, Long> {

}
