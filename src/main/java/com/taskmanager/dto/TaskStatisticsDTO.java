package com.taskmanager.dto;

/**
 * TaskStatisticsDTO - DTO для статистики задач
 * 
 * Возвращается на GET /api/voice/stats
 * 
 * Пример:
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
public class TaskStatisticsDTO {

    private Integer totalTasks;
    private Integer newTasks;
    private Integer inProgressTasks;
    private Integer completedTasks;
    private Integer cancelledTasks;
    private Integer activeTasks;
    private Integer overdueCount;

    // Constructors
    public TaskStatisticsDTO() {
    }

    public TaskStatisticsDTO(Integer totalTasks, Integer newTasks, Integer inProgressTasks,
                            Integer completedTasks, Integer cancelledTasks, 
                            Integer activeTasks, Integer overdueCount) {
        this.totalTasks = totalTasks;
        this.newTasks = newTasks;
        this.inProgressTasks = inProgressTasks;
        this.completedTasks = completedTasks;
        this.cancelledTasks = cancelledTasks;
        this.activeTasks = activeTasks;
        this.overdueCount = overdueCount;
    }

    // Getters и Setters
    public Integer getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Integer totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Integer getNewTasks() {
        return newTasks;
    }

    public void setNewTasks(Integer newTasks) {
        this.newTasks = newTasks;
    }

    public Integer getInProgressTasks() {
        return inProgressTasks;
    }

    public void setInProgressTasks(Integer inProgressTasks) {
        this.inProgressTasks = inProgressTasks;
    }

    public Integer getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(Integer completedTasks) {
        this.completedTasks = completedTasks;
    }

    public Integer getCancelledTasks() {
        return cancelledTasks;
    }

    public void setCancelledTasks(Integer cancelledTasks) {
        this.cancelledTasks = cancelledTasks;
    }

    public Integer getActiveTasks() {
        return activeTasks;
    }

    public void setActiveTasks(Integer activeTasks) {
        this.activeTasks = activeTasks;
    }

    public Integer getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(Integer overdueCount) {
        this.overdueCount = overdueCount;
    }

    @Override
    public String toString() {
        return "TaskStatisticsDTO{" +
                "totalTasks=" + totalTasks +
                ", newTasks=" + newTasks +
                ", inProgressTasks=" + inProgressTasks +
                ", completedTasks=" + completedTasks +
                ", cancelledTasks=" + cancelledTasks +
                ", activeTasks=" + activeTasks +
                ", overdueCount=" + overdueCount +
                '}';
    }
}