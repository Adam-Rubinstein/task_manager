package com.taskmanager.dto;

import com.taskmanager.model.Task;

public class VoiceTaskResponse {
    private boolean success;
    private String message;
    private Task task;
    private String error;

    public VoiceTaskResponse() {}

    public VoiceTaskResponse(boolean success, String message, Task task, String error) {
        this.success = success;
        this.message = message;
        this.task = task;
        this.error = error;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
