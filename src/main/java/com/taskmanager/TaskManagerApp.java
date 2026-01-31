package com.taskmanager;

import com.taskmanager.model.AppSettings;
import com.taskmanager.service.SettingsService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
@ComponentScan(basePackages = {"com.taskmanager"})
public class TaskManagerApp extends Application {

    private static ApplicationContext context;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Инициализируем Spring контекст ДО загрузки FXML
        context = SpringApplication.run(TaskManagerApp.class);

        // Загружаем настройки
        SettingsService settingsService = context.getBean(SettingsService.class);
        AppSettings settings = settingsService.getCurrentSettings();

        // Загружаем FXML с Factory из Spring контекста
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
        loader.setControllerFactory(context::getBean);

        Parent root = loader.load();

        // Применяем размер окна из настроек
        Scene scene = new Scene(root, settings.getWindowWidth(), settings.getWindowHeight());

        stage.setTitle("Voice Task Manager");
        stage.setScene(scene);

        // Сохранять размер окна при изменении
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                settingsService.updateWindowSize(newVal.doubleValue(), stage.getHeight());
            }
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                settingsService.updateWindowSize(stage.getWidth(), newVal.doubleValue());
            }
        });

        stage.show();
    }

    @Override
    public void stop() {
        // Корректное завершение Spring контекста при закрытии приложения
        if (context != null) {
            SpringApplication.exit(context);
        }
    }
}
