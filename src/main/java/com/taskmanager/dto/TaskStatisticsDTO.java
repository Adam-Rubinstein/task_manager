package com.taskmanager.dto;

public class TaskStatisticsDTO {
    private int totalTasks;
    private int newTasks;
    private int inProgressTasks;
    private int completedTasks;
    private int cancelledTasks;
    private int activeTasks;
    private int overdueCount;

    public TaskStatisticsDTO() {}

    public TaskStatisticsDTO(int totalTasks, int newTasks, int activeTasks, int completedTasks, int overdueCount) {
        this.totalTasks = totalTasks;
        this.newTasks = newTasks;
        this.activeTasks = activeTasks;
        this.completedTasks = completedTasks;
        this.overdueCount = overdueCount;
    }

    // Getters and Setters
    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getNewTasks() { return newTasks; }
    public void setNewTasks(int newTasks) { this.newTasks = newTasks; }

    public int getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getCancelledTasks() { return cancelledTasks; }
    public void setCancelledTasks(int cancelledTasks) { this.cancelledTasks = cancelledTasks; }

    public int getActiveTasks() { return activeTasks; }
    public void setActiveTasks(int activeTasks) { this.activeTasks = activeTasks; }

    public int getOverdueCount() { return overdueCount; }
    public void setOverdueCount(int overdueCount) { this.overdueCount = overdueCount; }

    @Override
    public String toString() {
        return "TaskStatisticsDTO{" +
                "totalTasks=" + totalTasks +
                ", newTasks=" + newTasks +
                ", activeTasks=" + activeTasks +
                ", completedTasks=" + completedTasks +
                ", overdueCount=" + overdueCount +
                '}';
    }
}
