package com.taskmanager.config;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * ThemeManager - управление светлой и тёмной темой приложения
 *
 * Функциональность:
 * - Детектирует системные настройки (светлая/тёмная тема ОС)
 * - Сохраняет выбор пользователя в application.properties
 * - Применяет CSS стили в зависимости от выбранной темы
 * - Предоставляет методы для переключения тем в runtime
 */
@Component
public class ThemeManager {

    public enum Theme {
        LIGHT("light-theme.css"),
        DARK("dark-theme.css");

        private final String cssFileName;

        Theme(String cssFileName) {
            this.cssFileName = cssFileName;
        }

        public String getCssFileName() {
            return cssFileName;
        }
    }

    private Theme currentTheme;
    private Scene currentScene;
    private Properties appProperties;
    private String propertiesPath = "application.properties";

    /**
     * Конструктор - загружает сохранённую тему или детектирует системную
     */
    public ThemeManager() {
        loadProperties();
        detectTheme();
    }

    /**
     * Загрузить свойства из application.properties
     */
    private void loadProperties() {
        appProperties = new Properties();
        try {
            appProperties.load(new FileReader(propertiesPath));
        } catch (IOException e) {
            // Файл не найден или недоступен, используем значения по умолчанию
            System.out.println("⚠️  Не удалось загрузить application.properties: " + e.getMessage());
        }
    }

    /**
     * Сохранить свойства в application.properties
     */
    private void saveProperties() {
        try {
            appProperties.store(new FileWriter(propertiesPath), "TaskManager Application Configuration");
        } catch (IOException e) {
            System.err.println("❌ Ошибка сохранения конфигурации: " + e.getMessage());
        }
    }

    /**
     * Детектировать тему: сначала из конфига, потом из системы
     */
    private void detectTheme() {
        // 1. Проверяем сохранённую тему в application.properties
        String savedTheme = appProperties.getProperty("app.theme");
        if (savedTheme != null) {
            try {
                currentTheme = Theme.valueOf(savedTheme.toUpperCase());
                System.out.println("✅ Загружена сохранённая тема: " + currentTheme);
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️  Неизвестная тема в конфиге: " + savedTheme);
            }
        }

        // 2. Детектируем системную тему (для Windows, macOS, Linux)
        detectSystemTheme();
    }

    /**
     * Детектировать системную тему ОС
     */
    private void detectSystemTheme() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            // Windows
            detectWindowsTheme();
        } else if (osName.contains("mac")) {
            // macOS
            detectMacTheme();
        } else if (osName.contains("linux")) {
            // Linux (обычно светлая по умолчанию)
            currentTheme = Theme.LIGHT;
            System.out.println("🐧 Система Linux, используется светлая тема");
        } else {
            // Неизвестная ОС, используем светлую
            currentTheme = Theme.LIGHT;
            System.out.println("❓ Неизвестная ОС, используется светлая тема по умолчанию");
        }
    }

    /**
     * Детектировать тему Windows из реестра
     */
    private void detectWindowsTheme() {
        try {
            // Запускаем cmd для чтения реестра Windows
            Process process = Runtime.getRuntime().exec(
                    new String[]{"reg", "query", "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", "/v", "AppsUseLightTheme"}
            );

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("0x0")) {
                    // 0x0 = тёмная тема
                    currentTheme = Theme.DARK;
                    System.out.println("🌙 Windows: обнаружена тёмная тема");
                    return;
                } else if (line.contains("0x1")) {
                    // 0x1 = светлая тема
                    currentTheme = Theme.LIGHT;
                    System.out.println("☀️ Windows: обнаружена светлая тема");
                    return;
                }
            }

            // Если не удалось прочитать, используем светлую по умолчанию
            currentTheme = Theme.LIGHT;
        } catch (Exception e) {
            // Если произойдёт ошибка, используем светлую тему
            currentTheme = Theme.LIGHT;
            System.out.println("⚠️  Не удалось детектировать тему Windows, используется светлая");
        }
    }

    /**
     * Детектировать тему macOS из системных настроек
     */
    private void detectMacTheme() {
        try {
            // На macOS используем AppleScript для чтения настроек
            Process process = Runtime.getRuntime().exec(
                    new String[]{"defaults", "read", "-g", "AppleInterfaceStyle"}
            );

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
            );

            String line = reader.readLine();
            if (line != null && line.contains("Dark")) {
                currentTheme = Theme.DARK;
                System.out.println("🌙 macOS: обнаружена тёмная тема");
            } else {
                currentTheme = Theme.LIGHT;
                System.out.println("☀️ macOS: обнаружена светлая тема");
            }
        } catch (Exception e) {
            // Если не удалось прочитать, используем светлую
            currentTheme = Theme.LIGHT;
            System.out.println("⚠️ Не удалось детектировать тему macOS, используется светлая");
        }
    }

    /**
     * Получить текущую тему
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Установить сцену для управления стилями
     */
    public void setScene(Scene scene) {
        this.currentScene = scene;
        applyTheme();
    }

    /**
     * Применить текущую тему к сцене
     */
    public void applyTheme() {
        if (currentScene == null) {
            System.out.println("⚠️ Сцена не установлена!");
            return;
        }

        Platform.runLater(() -> {
            // Очищаем старые стили
            currentScene.getStylesheets().clear();

            // Загружаем CSS файл текущей темы
            String cssResource = "/styles/" + currentTheme.getCssFileName();
            String resource = getClass().getResource(cssResource).toExternalForm();
            currentScene.getStylesheets().add(resource);

            System.out.println("✅ Применена тема: " + currentTheme);
        });
    }

    /**
     * Переключить тему (светлая ↔ тёмная)
     */
    public void toggleTheme() {
        currentTheme = currentTheme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT;
        saveThemePreference();
        applyTheme();
        System.out.println("🔄 Тема переключена на: " + currentTheme);
    }

    /**
     * Установить конкретную тему
     */
    public void setTheme(Theme theme) {
        if (currentTheme != theme) {
            currentTheme = theme;
            saveThemePreference();
            applyTheme();
            System.out.println("🎨 Установлена тема: " + currentTheme);
        }
    }

    /**
     * Сохранить предпочтение темы в application.properties
     */
    private void saveThemePreference() {
        appProperties.setProperty("app.theme", currentTheme.name());
        saveProperties();
    }

    /**
     * Получить CSS путь для текущей темы
     */
    public String getCurrentThemeCssPath() {
        return "/styles/" + currentTheme.getCssFileName();
    }

    /**
     * Проверить, является ли текущая тема тёмной
     */
    public boolean isDarkTheme() {
        return currentTheme == Theme.DARK;
    }

    /**
     * Проверить, является ли текущая тема светлой
     */
    public boolean isLightTheme() {
        return currentTheme == Theme.LIGHT;
    }
}