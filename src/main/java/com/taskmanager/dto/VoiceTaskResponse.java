package com.taskmanager.dto;

import com.taskmanager.model.Task;

/**
 * VoiceTaskResponse - DTO для ответа на запрос создания задачи
 * 
 * Отправляется обратно Telegram боту в формате JSON
 * 
 * Пример успешного ответа:
 * {
 *   "success": true,
 *   "message": "✅ Задача 'Купить молоко' создана на 02.01.2026 15:00",
 *   "task": {
 *     "id": 1,
 *     "title": "Купить молоко",
 *     "dueDate": "2026-01-02T15:00:00",
 *     "priority": 8,
 *     "status": "NEW"
 *   },
 *   "error": null
 * }
 * 
 * Пример ошибки:
 * {
 *   "success": false,
 *   "message": "Ошибка: текст не может быть пустым",
 *   "task": null,
 *   "error": "Empty text"
 * }
 */
public class VoiceTaskResponse {

    private Boolean success;
    private String message;
    private Task task;
    private String error;

    // Constructors
    public VoiceTaskResponse() {
    }

    public VoiceTaskResponse(Boolean success, String message, Task task, String error) {
        this.success = success;
        this.message = message;
        this.task = task;
        this.error = error;
    }

    // Getters и Setters
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "VoiceTaskResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", task=" + task +
                ", error='" + error + '\'' +
                '}';
    }
}