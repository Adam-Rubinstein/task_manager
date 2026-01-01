package com.taskmanager.dao;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByDueDateBetween(LocalDateTime start, LocalDateTime end);
    List<Task> findByPriorityGreaterThanOrderByDueDateAsc(Integer priority);
}
