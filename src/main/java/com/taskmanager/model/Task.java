package com.taskmanager.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Task - сущность задачи в системе
 */
@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description = "";

    @Column(nullable = true)
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.NEW;

    @Column(nullable = false)
    private Integer priority = 5;

    // ==================== НОВЫЕ ПОЛЯ ДЛЯ РЕКУРСИИ ====================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrenceType recurrenceType = RecurrenceType.NONE;

    @Column(nullable = false)
    private Integer recurrenceInterval = 0; // Дни для CUSTOM

    // ==================== Конструкторы ====================

    /**
     * ⭐ КОНСТРУКТОР ПО УМОЛЧАНИЮ (ОБЯЗАТЕЛЕН для Hibernate!)
     */
    public Task() {
        this.description = "";
        this.priority = 5;
        this.status = TaskStatus.NEW;
        this.recurrenceType = RecurrenceType.NONE;
        this.recurrenceInterval = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Конструктор с полными параметрами
     */
    public Task(String description, Integer priority, LocalDateTime dueDate, RecurrenceType recurrenceType) {
        this.description = description != null ? description : "";
        this.priority = priority != null ? priority : 5;
        this.dueDate = dueDate;
        this.recurrenceType = recurrenceType != null ? recurrenceType : RecurrenceType.NONE;
        this.recurrenceInterval = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = TaskStatus.NEW;
    }

    // ==================== БИЗНЕС-ЛОГИКА ====================

    /**
     * Извлечь название из первой строки описания
     */
    public String getTitle() {
        if (description == null || description.isEmpty()) {
            return "Без названия";
        }

        int newlineIndex = description.indexOf('\n');
        if (newlineIndex == -1) {
            return description;
        }

        return description.substring(0, newlineIndex).trim();
    }

    /**
     * Проверить, является ли задача просроченной
     */
    public boolean isOverdue() {
        if (dueDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueDate) && status != TaskStatus.COMPLETED;
    }

    /**
     * Проверить, является ли задача на сегодня или завтра
     */
    public boolean isTodayOrTomorrow() {
        if (dueDate == null) {  // Только проверяем дату, БЕЗ проверки статуса!
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrow = today.plusDays(1);
        LocalDateTime taskDay = dueDate.withHour(0).withMinute(0).withSecond(0);

        return (taskDay.equals(today) || taskDay.equals(tomorrow)) && status != TaskStatus.COMPLETED;
    }

    /**
     * Проверить, является ли задача на неделю
     */
    public boolean isThisWeek() {
        if (dueDate == null) {  // Только проверяем дату, БЕЗ проверки статуса!
            return false;
        }

        if (isOverdue() || isTodayOrTomorrow()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusDays(7);

        return !dueDate.isAfter(weekLater) && status != TaskStatus.COMPLETED;
    }

    /**
     * Имеет ли задача рекурсию
     */
    public boolean hasRecurrence() {
        return recurrenceType != null && !recurrenceType.equals(RecurrenceType.NONE);
    }

    // ==================== Getters and Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority != null ? priority : 5;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType != null ? recurrenceType : RecurrenceType.NONE;
    }

    public Integer getRecurrenceInterval() {
        return recurrenceInterval;
    }

    public void setRecurrenceInterval(Integer recurrenceInterval) {
        this.recurrenceInterval = recurrenceInterval != null ? recurrenceInterval : 0;
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + getTitle() + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", recurrenceType=" + recurrenceType +
                '}';
    }
}