package com.taskmanager.service;

import com.taskmanager.dao.TaskRepository;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // Создать новую задачу
    public Task createTask(String title, String description, LocalDateTime dueDate, Integer priority) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setPriority(priority);
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus(TaskStatus.NEW);

        return taskRepository.save(task);
    }

    // Получить задачу по ID
    public Optional<Task> getTask(Long id) {
        return taskRepository.findById(id);
    }

    // Получить все задачи
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Получить задачи по статусу
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    // Обновить статус задачи
    public Task updateTaskStatus(Long id, TaskStatus status) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            Task t = task.get();
            t.setStatus(status);
            return taskRepository.save(t);
        }
        return null;
    }

    // Удалить задачу
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    // Получить важные задачи (приоритет > 5)
    public List<Task> getImportantTasks() {
        return taskRepository.findByPriorityGreaterThanOrderByDueDateAsc(5);
    }

    // Получить задачи в диапазоне дат
    public List<Task> getTasksByDateRange(LocalDateTime start, LocalDateTime end) {
        return taskRepository.findByDueDateBetween(start, end);
    }

    // Обновить всю задачу (для редактирования из MainController)
    public Task updateTask(Task task) {
        if (task != null && task.getId() != null) {
            return taskRepository.save(task);
        }
        return null;
    }

    public List<Task> getActiveTasks() {
        return taskRepository.findAll().stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED &&
                        task.getStatus() != TaskStatus.CANCELLED)
                .toList();
    }

}
