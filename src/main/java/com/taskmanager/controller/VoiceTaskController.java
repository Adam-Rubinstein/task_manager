package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.VoiceParsingService;
import com.taskmanager.dto.VoiceTaskRequest;
import com.taskmanager.dto.VoiceTaskParsed;
import com.taskmanager.dto.VoiceTaskResponse;
import com.taskmanager.dto.TaskStatisticsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * VoiceTaskController - REST API для Voice Input (ФАЗА 2)
 * 
 * 7 endpoints для Telegram бота:
 * POST   /api/voice/create-task  - создание задачи из текста
 * GET    /api/voice/stats         - статистика
 * GET    /api/voice/today         - задачи на сегодня
 * GET    /api/voice/overdue       - просроченные
 * GET    /api/voice/list          - последние N задач
 * GET    /api/voice/search        - поиск по ключевому слову
 * GET    /api/voice/active        - активные задачи
 */
@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = "*", maxAge = 3600)
public class VoiceTaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private VoiceParsingService voiceParsingService;

    /**
     * POST /api/voice/create-task
     * Создание задачи из текста голосового сообщения
     * 
     * Request:
     * {
     *   "text": "Купить молоко завтра в 15:00, приоритет 8",
     *   "telegramUserId": 123456789
     * }
     * 
     * Response (успех):
     * {
     *   "success": true,
     *   "message": "✅ Задача 'Купить молоко' создана на 02.01.2026 15:00",
     *   "task": { id, title, dueDate, priority, status, ... }
     * }
     * 
     * Response (ошибка):
     * {
     *   "success": false,
     *   "message": "Ошибка",
     *   "error": "Текст не может быть пустым"
     * }
     */
    @PostMapping("/create-task")
    public ResponseEntity<VoiceTaskResponse> createTaskFromVoice(
            @RequestBody VoiceTaskRequest request) {

        try {
            // Валидация входных данных
            if (request == null || request.getText() == null || request.getText().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new VoiceTaskResponse(
                                false,
                                "Ошибка: текст не может быть пустым",
                                null,
                                "Empty text"
                        ));
            }

            // Парсинг текста
            VoiceTaskParsed parsed = voiceParsingService.parseVoiceText(request.getText());

            if (parsed == null || !voiceParsingService.isValidParsed(parsed)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new VoiceTaskResponse(
                                false,
                                "Ошибка: не удалось распарсить текст",
                                null,
                                "Invalid parsed data"
                        ));
            }

            // Создание задачи
            Task createdTask = taskService.createTaskFromVoice(parsed);

            if (createdTask == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new VoiceTaskResponse(
                                false,
                                "Ошибка: не удалось создать задачу",
                                null,
                                "Failed to save task"
                        ));
            }

            // Успешный ответ
            String dateStr = createdTask.getDueDate() != null
                    ? createdTask.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                    : "неизвестно";
            
            String message = String.format(
                    "✅ Задача '%s' создана на %s\\n🔴 Приоритет: %d",
                    createdTask.getTitle(),
                    dateStr,
                    createdTask.getPriority()
            );

            return ResponseEntity.ok(new VoiceTaskResponse(
                    true,
                    message,
                    createdTask,
                    null
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VoiceTaskResponse(
                            false,
                            "Ошибка сервера: " + e.getMessage(),
                            null,
                            e.getClass().getName()
                    ));
        }
    }

    /**
     * GET /api/voice/stats
     * Получить статистику по задачам
     * 
     * Response:
     * {
     *   "totalTasks": 5,
     *   "newTasks": 2,
     *   "inProgressTasks": 1,
     *   "completedTasks": 2,
     *   "cancelledTasks": 0,
     *   "activeTasks": 3,
     *   "overdueCount": 0
     * }
     */
    @GetMapping("/stats")
    public ResponseEntity<TaskStatisticsDTO> getStatistics() {
        try {
            TaskStatisticsDTO stats = new TaskStatisticsDTO();
            stats.setTotalTasks(taskService.getTotalTaskCount());
            stats.setNewTasks(taskService.getNewTaskCount());
            stats.setCompletedTasks(taskService.getCompletedTaskCount());
            stats.setActiveTasks(taskService.getActiveTaskCount());
            stats.setOverdueCount(taskService.getOverdueTaskCount());
            stats.setInProgressTasks(0); // Пока нет, можно добавить метод если надо
            stats.setCancelledTasks(0);  // Пока нет, можно добавить метод если надо

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/voice/today
     * Получить задачи на сегодня
     * 
     * Response:
     * [
     *   { id: 1, title: "...", dueDate: "...", priority: 8, status: "NEW" },
     *   ...
     * ]
     */
    @GetMapping("/today")
    public ResponseEntity<List<Task>> getTasksForToday() {
        try {
            List<Task> tasks = taskService.getTasksForToday();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/voice/overdue
     * Получить просроченные задачи
     * 
     * Response:
     * [
     *   { id: 1, title: "...", dueDate: "...", priority: 8, status: "NEW" },
     *   ...
     * ]
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        try {
            List<Task> tasks = taskService.getOverdueTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/voice/list?limit=10
     * Получить последние N задач
     * 
     * Parameters:
     * - limit (optional, default 10): количество задач
     * 
     * Response:
     * [
     *   { id: 1, title: "...", dueDate: "...", priority: 8, status: "NEW" },
     *   ...
     * ]
     */
    @GetMapping("/list")
    public ResponseEntity<List<Task>> getTaskList(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Task> tasks = taskService.getLatestTasks(limit);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/voice/search?q=молоко
     * Поиск задач по ключевому слову
     * 
     * Parameters:
     * - q (required): ключевое слово для поиска
     * 
     * Response:
     * [
     *   { id: 1, title: "Купить молоко", dueDate: "...", priority: 8, status: "NEW" },
     *   ...
     * ]
     */
    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchTasks(
            @RequestParam String q) {
        try {
            if (q == null || q.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            List<Task> tasks = taskService.searchTasks(q);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/voice/active
     * Получить активные (не завершённые) задачи
     * 
     * Response:
     * [
     *   { id: 1, title: "...", dueDate: "...", priority: 8, status: "NEW" },
     *   ...
     * ]
     */
    @GetMapping("/active")
    public ResponseEntity<List<Task>> getActiveTasks() {
        try {
            List<Task> tasks = taskService.getActiveTasksForApi();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("✅ Voice Task API is running");
    }
}