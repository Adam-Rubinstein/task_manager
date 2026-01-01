package com.taskmanager.dao;

import com.taskmanager.model.Alert;
import com.taskmanager.model.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByTaskId(Long taskId);
    List<Alert> findByType(AlertType type);
    List<Alert> findByIsReadFalse();
    List<Alert> findByAlertTimeBetween(LocalDateTime start, LocalDateTime end);
}
