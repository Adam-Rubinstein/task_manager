package com.taskmanager.dto;

import java.time.LocalDateTime;

public class VoiceTaskParsed {
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Integer priority;
    private Boolean isUrgent;

    public VoiceTaskParsed() {}

    public VoiceTaskParsed(String title, String description, LocalDateTime dueDate, Integer priority, Boolean isUrgent) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.isUrgent = isUrgent;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Boolean getIsUrgent() { return isUrgent; }
    public void setIsUrgent(Boolean isUrgent) { this.isUrgent = isUrgent; }

    @Override
    public String toString() {
        return "VoiceTaskParsed{" +
                "title='" + title + '\'' +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", isUrgent=" + isUrgent +
                '}';
    }
}
