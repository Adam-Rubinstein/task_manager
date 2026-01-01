package com.taskmanager.service;

import com.taskmanager.dao.AlertRepository;
import com.taskmanager.model.Alert;
import com.taskmanager.model.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    // Создать оповещение
    public Alert createAlert(Long taskId, LocalDateTime alertTime, AlertType type, String message) {
        Alert alert = new Alert();
        alert.setTaskId(taskId);
        alert.setAlertTime(alertTime);
        alert.setType(type);
        alert.setMessage(message);
        alert.setIsRead(false);

        return alertRepository.save(alert);
    }

    // Получить все непрочитанные оповещения
    public List<Alert> getUnreadAlerts() {
        return alertRepository.findByIsReadFalse();
    }

    // Получить оповещения по задаче
    public List<Alert> getAlertsByTask(Long taskId) {
        return alertRepository.findByTaskId(taskId);
    }

    // Получить оповещения по типу
    public List<Alert> getAlertsByType(AlertType type) {
        return alertRepository.findByType(type);
    }

    // Отметить как прочитано
    public Alert markAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId).orElse(null);
        if (alert != null) {
            alert.setIsRead(true);
            return alertRepository.save(alert);
        }
        return null;
    }

    // Удалить оповещение
    public void deleteAlert(Long alertId) {
        alertRepository.deleteById(alertId);
    }
}
