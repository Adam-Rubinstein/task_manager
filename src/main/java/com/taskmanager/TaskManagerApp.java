package com.taskmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.taskmanager"})
public class TaskManagerApp extends javafx.application.Application {

    private static ConfigurableApplicationContext context;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/main-view.fxml")
            );
            loader.setControllerFactory(context::getBean);

            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 700);

            primaryStage.setTitle("Voice Task Manager");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка загрузки FXML: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        context = SpringApplication.run(TaskManagerApp.class, args);
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        if (context != null) {
            context.close();
        }
        super.stop();
    }
}
