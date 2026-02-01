package com.taskmanager.service;

import com.taskmanager.dao.AlertRepository;
import com.taskmanager.model.Alert;
import com.taskmanager.model.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    @Autowired
    private AlertRepository alertRepository;

    // Создать оповещение
    public Alert createAlert(Long taskId, LocalDateTime alertTime, AlertType type, String message) {
        log.debug("Создание оповещения: taskId={}, type={}, alertTime={}", taskId, type, alertTime);

        Alert alert = new Alert();
        alert.setTaskId(taskId);
        alert.setAlertTime(alertTime);
        alert.setType(type);
        alert.setMessage(message);
        alert.setIsRead(false);

        Alert savedAlert = alertRepository.save(alert);
        log.info("Оповещение создано: id={}, taskId={}", savedAlert.getId(), taskId);

        return savedAlert;
    }

    // Получить все непрочитанные оповещения
    public List<Alert> getUnreadAlerts() {
        log.debug("Загрузка непрочитанных оповещений");
        List<Alert> alerts = alertRepository.findByIsReadFalse();
        log.debug("Найдено непрочитанных оповещений: {}", alerts.size());
        return alerts;
    }

    // Получить оповещения по задаче
    public List<Alert> getAlertsByTask(Long taskId) {
        log.debug("Загрузка оповещений для задачи: taskId={}", taskId);
        return alertRepository.findByTaskId(taskId);
    }

    // Получить оповещения по типу
    public List<Alert> getAlertsByType(AlertType type) {
        log.debug("Загрузка оповещений типа: {}", type);
        return alertRepository.findByType(type);
    }

    // Отметить как прочитано
    public Alert markAsRead(Long alertId) {
        log.debug("Отметка оповещения как прочитанного: alertId={}", alertId);

        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setIsRead(true);
                    Alert updated = alertRepository.save(alert);
                    log.info("Оповещение отмечено как прочитанное: alertId={}", alertId);
                    return updated;
                })
                .orElseThrow(() -> {
                    log.error("Оповещение не найдено: alertId={}", alertId);
                    return new IllegalArgumentException("Alert not found: " + alertId);
                });
    }

    // Удалить оповещение
    public void deleteAlert(Long alertId) {
        log.debug("Удаление оповещения: alertId={}", alertId);
        alertRepository.deleteById(alertId);
        log.info("Оповещение удалено: alertId={}", alertId);
    }
}
