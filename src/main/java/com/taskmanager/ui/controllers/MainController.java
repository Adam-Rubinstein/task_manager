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
import javafx.scene.input.MouseEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class MainController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AudioFileService audioFileService;

    // ==================== ФОРМАТЕР ДАТЫ ====================
    private static final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ==================== UI COMPONENTS ====================

    @FXML
    private TextField taskNameInput;

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
    private TableColumn<Task, String> dueDateColumn;

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

        // ✅ НОВЫЙ ФОРМАТЕР ДАТЫ
        dueDateColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            if (task.getDueDate() != null) {
                String formattedDate = task.getDueDate().format(dateFormatter);
                return new javafx.beans.property.SimpleStringProperty(formattedDate);
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

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
     * ✅ АВТОЗАПОЛНЕНИЕ: если название пусто, берём первую строку описания
     */
    @FXML
    private void handleCreateTask() {
        String title = taskNameInput.getText().trim();
        String description = taskDescriptionInput.getText().trim();
        Integer priority = prioritySpinner.getValue();
        LocalDateTime dueDate = dueDatePicker.getValue() != null
                ? dueDatePicker.getValue().atStartOfDay()
                : LocalDateTime.now().plusDays(1);
        RecurrenceType recurrenceType = recurrenceCombo.getValue() != null
                ? recurrenceCombo.getValue()
                : RecurrenceType.NONE;

        // ✅ НОВАЯ ЛОГИКА: если название пусто, берём первую строку из описания
        if (title.isEmpty()) {
            if (description.isEmpty()) {
                showAlert("Ошибка", "Введите описание задачи! Название будет автозаполнено из первой строки.");
                return;
            }
            // Берём первую строку описания как название
            String[] lines = description.split("\n");
            title = lines[0].trim();
            if (title.isEmpty()) {
                showAlert("Ошибка", "Первая строка описания пуста! Введите название или описание.");
                return;
            }
        }

        // Если описание пусто, используем название
        if (description.isEmpty()) {
            description = title;
        }

        try {
            Task newTask = taskService.createTask(
                    title,
                    description,
                    priority,
                    dueDate,
                    recurrenceType
            );

            tasksList.add(newTask);

            // Очистить форму после успешного создания
            taskNameInput.clear();
            taskDescriptionInput.clear();
            prioritySpinner.getValueFactory().setValue(5);
            dueDatePicker.setValue(null);
            recurrenceCombo.setValue(RecurrenceType.NONE);
            intervalSpinner.getValueFactory().setValue(7);

            showAlert("Успех", "Задача создана!\nНазвание: " + title);

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
     * ✅ НОВЫЙ МЕТОД: Открыть задачу в отдельном окне по клику
     */
    @FXML
    private void handleTaskClick(MouseEvent event) {
        // Двойной клик (clickCount = 2)
        if (event.getClickCount() == 2) {
            Task selected = tasksTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openTaskDetailWindow(selected);
            }
        }
    }

    /**
     * ✅ НОВЫЙ МЕТОД: Открыть окно с деталями задачи
     */
    private void openTaskDetailWindow(Task task) {
        try {
            // Создаём новое окно
            javafx.stage.Stage detailStage = new javafx.stage.Stage();
            detailStage.setTitle("Задача: " + task.getTitle());
            detailStage.setWidth(500);
            detailStage.setHeight(400);

            // Создаём VBox с информацией о задаче
            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
            vbox.setStyle("-fx-padding: 15; -fx-font-size: 12;");

            // Заголовок
            Label titleLabel = new Label("Название: " + task.getTitle());
            titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

            // Описание
            Label descLabel = new Label("Описание:");
            TextArea descArea = new TextArea(task.getDescription());
            descArea.setWrapText(true);
            descArea.setPrefHeight(100);
            descArea.setEditable(false);

            // Статус
            Label statusLabel = new Label("Статус: " + task.getStatus());

            // Приоритет
            Label priorityLabel = new Label("Приоритет: " + task.getPriority() + "/10");

            // Дата выполнения
            String dueDateStr = task.getDueDate() != null
                    ? task.getDueDate().format(dateFormatter)
                    : "Не установлена";
            Label dueDateLabel = new Label("Срок выполнения: " + dueDateStr);

            // Тип повтора
            Label recurrenceLabel = new Label("Тип повтора: " + task.getRecurrenceType());

            // Интервал повтора
            Label intervalLabel = new Label("Интервал повтора: " + task.getRecurrenceInterval() + " дней");

            // Кнопка закрытия
            Button closeButton = new Button("Закрыть");
            closeButton.setStyle("-fx-padding: 8; -fx-font-size: 12;");
            closeButton.setOnAction(e -> detailStage.close());

            // Добавляем всё в VBox
            vbox.getChildren().addAll(
                    titleLabel,
                    new Separator(),
                    descLabel,
                    descArea,
                    new Separator(),
                    statusLabel,
                    priorityLabel,
                    dueDateLabel,
                    recurrenceLabel,
                    intervalLabel,
                    new Separator(),
                    closeButton
            );

            // Создаём ScrollPane для прокрутки
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(vbox);
            scrollPane.setFitToWidth(true);

            // Создаём сцену
            javafx.scene.Scene scene = new javafx.scene.Scene(scrollPane);
            detailStage.setScene(scene);

            // Показываем окно
            detailStage.show();

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось открыть задачу: " + e.getMessage());
        }
    }

    /**
     * Обновить задачу (двойной клик по строке)
     */
    @FXML
    private void handleTaskDoubleClick() {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            taskNameInput.setText(selected.getTitle());
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
        TaskStatus selected = (TaskStatus) statusFilter.getValue();
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
        int selectedIndex = alertsListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            List<Alert> unread = alertService.getUnreadAlerts();
            if (selectedIndex < unread.size()) {
                Alert alert = unread.get(selectedIndex);
                alertService.markAsRead(alert.getId());
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
     * Показать диалоговое окно
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
