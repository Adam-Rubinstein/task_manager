package com.taskmanager.service;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.RecurrenceType;
import com.taskmanager.dao.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Получить все задачи
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Получить задачу по ID
     */
    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    /**
     * Получить задачи по статусу
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    /**
     * Создать новую задачу
     */
    public Task createTask(String title, String description, Integer priority, LocalDateTime dueDate, RecurrenceType recurrenceType) {
        // Объединяем title и description в одно поле
        String fullDescription = (title != null ? title : "Без названия") + "\n" + (description != null ? description : "");

        Task task = new Task(fullDescription, priority, dueDate, recurrenceType);
        return taskRepository.save(task);
    }

    /**
     * Сохранить задачу
     */
    public Task saveTask(Task task) {
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    /**
     * Обновить задачу (по ID и отдельным параметрам)
     */
    public Task updateTask(Long id, String description, Integer priority, LocalDateTime dueDate, TaskStatus status, RecurrenceType recurrenceType) {
        Task task = getTaskById(id);
        if (task != null) {
            task.setDescription(description);
            task.setPriority(priority);
            task.setDueDate(dueDate);
            task.setStatus(status);
            task.setRecurrenceType(recurrenceType);
            task.setUpdatedAt(LocalDateTime.now());
            return taskRepository.save(task);
        }
        return null;
    }

    /**
     * Обновить задачу (принимает объект Task целиком)
     */
    public Task updateTask(Task task) {
        if (task != null && task.getId() != null) {
            task.setUpdatedAt(LocalDateTime.now());
            return taskRepository.save(task);
        }
        return null;
    }

    /**
     * Удалить задачу
     */
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    /**
     * Завершить задачу
     */
    public Task completeTask(Long id) {
        Task task = getTaskById(id);
        if (task != null) {
            task.setStatus(TaskStatus.COMPLETED);
            return saveTask(task);
        }
        return null;
    }

    /**
     * Вычислить следующую дату рекурсии
     */
    public LocalDateTime getNextRecurrenceDate(Task task) {
        LocalDateTime currentDate = task.getDueDate();
        if (currentDate == null) {
            currentDate = LocalDateTime.now();
        }

        return switch (task.getRecurrenceType()) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case CUSTOM -> currentDate.plusDays(task.getRecurrenceInterval() > 0 ? task.getRecurrenceInterval() : 7);
            default -> currentDate.plusDays(1);
        };
    }

    /**
     * Создать следующую копию рекурсивной задачи
     */
    public Task createNextRecurrence(Task completedTask) {
        if (!completedTask.hasRecurrence()) {
            return null;
        }

        LocalDateTime nextDate = getNextRecurrenceDate(completedTask);
        Task nextTask = new Task(completedTask.getDescription(), completedTask.getPriority(),
                nextDate, completedTask.getRecurrenceType());
        nextTask.setRecurrenceInterval(completedTask.getRecurrenceInterval());
        nextTask.setStatus(TaskStatus.NEW);
        return taskRepository.save(nextTask);
    }
}