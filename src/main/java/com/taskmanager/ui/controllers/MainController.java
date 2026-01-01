package com.taskmanager.ui.controllers;

import com.taskmanager.service.TaskService;
import com.taskmanager.service.AlertService;
import com.taskmanager.service.AudioFileService;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.RecurrenceType;
import com.taskmanager.model.Alert;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class MainController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AudioFileService audioFileService;

    // ==================== UI COMPONENTS ====================

    @FXML
    private TextField taskTitleInput;

    @FXML
    private TextArea taskDescriptionInput;

    @FXML
    private Spinner<Integer> prioritySpinner;

    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private ComboBox<RecurrenceType> recurrenceCombo;

    @FXML
    private Spinner<Integer> intervalSpinner;

    @FXML
    private TableView<Task> tasksTable;

    @FXML
    private TableColumn<Task, String> titleColumn;

    @FXML
    private TableColumn<Task, TaskStatus> statusColumn;

    @FXML
    private TableColumn<Task, Integer> priorityColumn;

    @FXML
    private TableColumn<Task, LocalDateTime> dueDateColumn;

    @FXML
    private Button createTaskButton;

    @FXML
    private Button deleteTaskButton;

    @FXML
    private ComboBox<TaskStatus> statusFilter;

    @FXML
    private Label alertsCountLabel;

    @FXML
    private ListView<String> alertsListView;

    private ObservableList<Task> tasksList;

    // ==================== ИНИЦИАЛИЗАЦИЯ ====================

    @FXML
    public void initialize() {
        // Инициализация Spinner для приоритета
        prioritySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 5)
        );

        // Инициализация Spinner для интервала повтора
        intervalSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 7)
        );

        // Инициализация ComboBox для типа повтора
        recurrenceCombo.setItems(FXCollections.observableArrayList(RecurrenceType.values()));
        recurrenceCombo.setValue(RecurrenceType.NONE);

        // Инициализация ComboBox для фильтра статуса
        statusFilter.setItems(FXCollections.observableArrayList(TaskStatus.values()));

        // Инициализация таблицы задач
        tasksList = FXCollections.observableArrayList();
        tasksTable.setItems(tasksList);

        // Привязка колонок таблицы
        titleColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle())
        );

        statusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStatus())
        );

        priorityColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPriority())
        );

        dueDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDueDate())
        );

        // Загрузить все задачи при запуске
        loadAllTasks();
        updateAlertsCount();

        // Обновлять оповещения каждые 10 секунд
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                    Platform.runLater(this::updateAlertsCount);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    // ==================== ОБРАБОТЧИКИ СОБЫТИЙ ====================

    /**
     * Создать новую задачу
     * ✅ ИСПРАВЛЕНО: правильный порядок параметров
     */
    @FXML
    private void handleCreateTask() {
        String description = taskDescriptionInput.getText().trim();
        Integer priority = prioritySpinner.getValue();
        LocalDateTime dueDate = dueDatePicker.getValue() != null
                ? dueDatePicker.getValue().atStartOfDay()
                : LocalDateTime.now().plusDays(1);
        RecurrenceType recurrenceType = recurrenceCombo.getValue() != null
                ? recurrenceCombo.getValue()
                : RecurrenceType.NONE;

        if (description.isEmpty()) {
            showAlert("Ошибка", "Введите описание задачи!");
            return;
        }

        try {
            // ✅ ИСПРАВЛЕННЫЙ ПОРЯДОК ПАРАМЕТРОВ:
            // createTask(String description, Integer priority, LocalDateTime dueDate, RecurrenceType recurrenceType)
            Task newTask = taskService.createTask(
                    description,      // 1. String description
                    priority,         // 2. Integer priority
                    dueDate,          // 3. LocalDateTime dueDate
                    recurrenceType    // 4. RecurrenceType recurrenceType
            );

            tasksList.add(newTask);
            taskDescriptionInput.clear();
            prioritySpinner.getValueFactory().setValue(5);
            dueDatePicker.setValue(null);
            recurrenceCombo.setValue(RecurrenceType.NONE);
            intervalSpinner.getValueFactory().setValue(7);
            showAlert("Успех", "Задача создана!");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось создать задачу: " + e.getMessage());
        }
    }

    /**
     * Удалить выбранную задачу
     */
    @FXML
    private void handleDeleteTask() {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите задачу для удаления!");
            return;
        }

        try {
            taskService.deleteTask(selected.getId());
            tasksList.remove(selected);
            showAlert("Успех", "Задача удалена!");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось удалить задачу: " + e.getMessage());
        }
    }

    /**
     * Обновить задачу
     */
    @FXML
    private void handleTaskDoubleClick() {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Заполнить форму выбранной задачей
            taskDescriptionInput.setText(selected.getDescription());
            prioritySpinner.getValueFactory().setValue(selected.getPriority());
            if (selected.getDueDate() != null) {
                dueDatePicker.setValue(selected.getDueDate().toLocalDate());
            }
            recurrenceCombo.setValue(selected.getRecurrenceType());
            intervalSpinner.getValueFactory().setValue(selected.getRecurrenceInterval());
        }
    }

    /**
     * Фильтровать задачи по статусу
     */
    @FXML
    private void handleFilterByStatus() {
        TaskStatus selected = statusFilter.getValue();
        if (selected == null) {
            loadAllTasks();
            return;
        }

        try {
            List<Task> filtered = taskService.getTasksByStatus(selected);
            tasksList.clear();
            tasksList.addAll(filtered);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось отфильтровать задачи: " + e.getMessage());
        }
    }

    /**
     * Обработка изменения типа повтора
     */
    @FXML
    private void handleRecurrenceChange() {
        RecurrenceType selected = recurrenceCombo.getValue();
        if (selected == null || selected == RecurrenceType.NONE) {
            intervalSpinner.setDisable(true);
        } else {
            intervalSpinner.setDisable(false);
        }
    }

    /**
     * Пометить оповещение как прочитанное
     */
    @FXML
    private void handleMarkAlertAsRead() {
        int selected = alertsListView.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            List<Alert> unread = alertService.getUnreadAlerts();
            if (selected < unread.size()) {
                alertService.markAsRead(unread.get(selected).getId());
                updateAlertsCount();
            }
        }
    }

    /**
     * Выход из приложения
     */
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Загрузить все задачи из БД
     */
    private void loadAllTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            tasksList.clear();
            tasksList.addAll(tasks);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось загрузить задачи: " + e.getMessage());
        }
    }

    /**
     * Обновить количество и список оповещений
     */
    private void updateAlertsCount() {
        try {
            List<Alert> unread = alertService.getUnreadAlerts();
            alertsCountLabel.setText("Оповещения: " + unread.size());
            ObservableList<String> alertsStrings = FXCollections.observableArrayList();
            for (Alert alert : unread) {
                alertsStrings.add(alert.getMessage() + " [" + alert.getType() + "]");
            }
            alertsListView.setItems(alertsStrings);
        } catch (Exception e) {
            alertsCountLabel.setText("Ошибка загрузки оповещений");
        }
    }

    /**
     * ⭐ ИСПРАВЛЕННЫЙ МЕТОД - Показать диалоговое окно
     * Использует полное имя javafx.scene.control.Alert чтобы избежать конфликта с моделью Alert
     */
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert jfxAlert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION
        );
        jfxAlert.setTitle(title);
        jfxAlert.setHeaderText(null);
        jfxAlert.setContentText(message);
        jfxAlert.showAndWait();
    }
}