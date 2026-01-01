package com.taskmanager.service;

import com.taskmanager.dao.TaskRepository;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.dto.VoiceTaskParsed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TaskService - Бизнес-логика для управления задачами
 * ФАЗА 1: CRUD операции
 * ФАЗА 2: Парсинг голосовых сообщений через Telegram
 */
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // ==================== ФАЗА 1: CRUD ====================

    /**
     * Создать новую задачу (ФАЗА 1)
     */
    public Task createTask(String title, String description, LocalDateTime dueDate, Integer priority) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setPriority(priority != null ? priority : 5);
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus(TaskStatus.NEW);
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    /**
     * Получить задачу по ID
     */
    public Optional<Task> getTask(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * Получить все задачи
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Получить задачи по статусу
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    /**
     * Обновить статус задачи
     */
    public Task updateTaskStatus(Long id, TaskStatus status) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            Task t = task.get();
            t.setStatus(status);
            t.setUpdatedAt(LocalDateTime.now());
            return taskRepository.save(t);
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
     * Получить важные задачи (приоритет > 5)
     */
    public List<Task> getImportantTasks() {
        return taskRepository.findByPriorityGreaterThanOrderByDueDateAsc(5);
    }

    /**
     * Получить задачи в диапазоне дат
     */
    public List<Task> getTasksByDateRange(LocalDateTime start, LocalDateTime end) {
        return taskRepository.findByDueDateBetween(start, end);
    }

    /**
     * Обновить всю задачу
     */
    public Task updateTask(Task task) {
        if (task != null && task.getId() != null) {
            task.setUpdatedAt(LocalDateTime.now());
            return taskRepository.save(task);
        }
        return null;
    }

    /**
     * Получить активные задачи (не завершены и не отменены)
     */
    public List<Task> getActiveTasks() {
        return taskRepository.findAll().stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED &&
                        task.getStatus() != TaskStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    // ==================== ФАЗА 2: VOICE INPUT ====================

    /**
     * Создать задачу из распарсенного голосового текста (ФАЗА 2)
     * @param parsed Распарсенные данные (VoiceParsingService)
     * @return Созданная задача
     */
    public Task createTaskFromVoice(VoiceTaskParsed parsed) {
        if (parsed == null || parsed.getTitle() == null || parsed.getTitle().isEmpty()) {
            return null;
        }

        Task task = new Task();
        task.setTitle(parsed.getTitle());
        task.setDescription(parsed.getDescription());
        task.setDueDate(parsed.getDueDate() != null ? parsed.getDueDate() : LocalDateTime.now().plusDays(1));
        task.setPriority(parsed.getPriority() != null ? parsed.getPriority() : 5);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setStatus(TaskStatus.NEW);

        return taskRepository.save(task);
    }

    /**
     * Получить задачи на сегодня (ФАЗА 2)
     */
    public List<Task> getTasksForToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1);
        
        return taskRepository.findByDueDateBetween(startOfDay, endOfDay).stream()
                .filter(task -> task.getStatus() != TaskStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    /**
     * Получить просроченные задачи (ФАЗА 2)
     */
    public List<Task> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        
        return taskRepository.findAll().stream()
                .filter(task -> task.getDueDate().isBefore(now) &&
                        task.getStatus() != TaskStatus.COMPLETED &&
                        task.getStatus() != TaskStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    /**
     * Получить активные задачи (не завершены) для API (ФАЗА 2)
     */
    public List<Task> getActiveTasksForApi() {
        return getActiveTasks();
    }

    /**
     * Получить последние N задач (ФАЗА 2)
     */
    public List<Task> getLatestTasks(int limit) {
        return taskRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Поиск задач по ключевому слову (ФАЗА 2)
     */
    public List<Task> searchTasks(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return getAllTasks();
        }

        String lowerKeyword = keyword.toLowerCase();
        return taskRepository.findAll().stream()
                .filter(task -> task.getTitle().toLowerCase().contains(lowerKeyword) ||
                        (task.getDescription() != null && 
                         task.getDescription().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }

    /**
     * Получить статистику для API (ФАЗА 2)
     */
    public int getTotalTaskCount() {
        return (int) taskRepository.count();
    }

    /**
     * Получить количество новых задач (ФАЗА 2)
     */
    public int getNewTaskCount() {
        return getTasksByStatus(TaskStatus.NEW).size();
    }

    /**
     * Получить количество завершённых задач (ФАЗА 2)
     */
    public int getCompletedTaskCount() {
        return getTasksByStatus(TaskStatus.COMPLETED).size();
    }

    /**
     * Получить количество активных задач (ФАЗА 2)
     */
    public int getActiveTaskCount() {
        return getActiveTasks().size();
    }

    /**
     * Получить количество просроченных задач (ФАЗА 2)
     */
    public int getOverdueTaskCount() {
        return getOverdueTasks().size();
    }
}