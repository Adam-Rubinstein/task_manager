package com.taskmanager.model;

/**
 * RecurrenceType - тип повторения задачи
 */
public enum RecurrenceType {
    NONE("Без повтора"),
    DAILY("Ежедневно"),
    WEEKLY("Еженедельно"),
    MONTHLY("Ежемесячно"),
    CUSTOM("Произвольный период");

    private final String displayName;

    RecurrenceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}