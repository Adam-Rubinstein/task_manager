package com.taskmanager;

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
@ComponentScan(basePackages = {"com.taskmanager"})  // ← ВАЖНО!
public class TaskManagerApp extends Application {

    private static ApplicationContext context;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Инициализируем Spring контекст ДО загрузки FXML
        context = SpringApplication.run(TaskManagerApp.class);

        // Загружаем FXML с Factory из Spring контекста
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
        loader.setControllerFactory(context::getBean);  // ← КЛЮЧЕВАЯ СТРОКА!

        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Voice Task Manager");
        stage.setScene(scene);
        stage.show();
    }
}
