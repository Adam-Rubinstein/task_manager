package com.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Модель настроек приложения для сохранения в JSON
 */
public class AppSettings implements Serializable {

    @JsonProperty("theme")
    private String theme; // "LIGHT" или "DARK"

    @JsonProperty("window_width")
    private double windowWidth;

    @JsonProperty("window_height")
    private double windowHeight;

    @JsonProperty("auto_save_interval")
    private int autoSaveInterval; // секунды

    @JsonProperty("default_priority")
    private int defaultPriority;

    // Конструктор по умолчанию
    public AppSettings() {
        this.theme = "LIGHT";
        this.windowWidth = 800.0;
        this.windowHeight = 600.0;
        this.autoSaveInterval = 10;
        this.defaultPriority = 5;
    }

    // Геттеры и сеттеры
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public double getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(double windowWidth) {
        this.windowWidth = windowWidth;
    }

    public double getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(double windowHeight) {
        this.windowHeight = windowHeight;
    }

    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }

    public void setAutoSaveInterval(int autoSaveInterval) {
        this.autoSaveInterval = autoSaveInterval;
    }

    public int getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(int defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    @Override
    public String toString() {
        return "AppSettings{" +
                "theme='" + theme + '\'' +
                ", windowWidth=" + windowWidth +
                ", windowHeight=" + windowHeight +
                ", autoSaveInterval=" + autoSaveInterval +
                ", defaultPriority=" + defaultPriority +
                '}';
    }
}
