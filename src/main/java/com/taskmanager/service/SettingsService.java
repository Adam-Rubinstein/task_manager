package com.taskmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.taskmanager.model.AppSettings;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Сервис для работы с настройками приложения (JSON)
 */
@Service
public class SettingsService {

    private static final String SETTINGS_FILE = "settings.json";
    private final ObjectMapper objectMapper;
    private AppSettings currentSettings;

    public SettingsService() {
        this.objectMapper = new ObjectMapper();
        // Красивое форматирование JSON
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.currentSettings = loadSettings();
    }

    /**
     * Загрузить настройки из JSON файла
     */
    public AppSettings loadSettings() {
        File file = new File(SETTINGS_FILE);

        if (!file.exists()) {
            System.out.println("Файл настроек не найден, создаём новый с настройками по умолчанию");
            currentSettings = new AppSettings();
            saveSettings(currentSettings);
            return currentSettings;
        }

        try {
            currentSettings = objectMapper.readValue(file, AppSettings.class);
            System.out.println("Настройки загружены: " + currentSettings);
            return currentSettings;
        } catch (IOException e) {
            System.err.println("Ошибка чтения настроек: " + e.getMessage());
            currentSettings = new AppSettings();
            return currentSettings;
        }
    }

    /**
     * Сохранить настройки в JSON файл
     */
    public void saveSettings(AppSettings settings) {
        try {
            objectMapper.writeValue(new File(SETTINGS_FILE), settings);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения настроек: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Получить текущие настройки
     */
    public AppSettings getCurrentSettings() {
        return currentSettings;
    }

    /**
     * Обновить тему
     */
    public void updateTheme(String theme) {
        currentSettings.setTheme(theme);
        saveSettings(currentSettings);
    }

    /**
     * Обновить размер окна
     */
    public void updateWindowSize(double width, double height) {
        currentSettings.setWindowWidth(width);
        currentSettings.setWindowHeight(height);
        saveSettings(currentSettings);
    }

    /**
     * Обновить приоритет по умолчанию
     */
    public void updateDefaultPriority(int priority) {
        currentSettings.setDefaultPriority(priority);
        saveSettings(currentSettings);
    }
}
