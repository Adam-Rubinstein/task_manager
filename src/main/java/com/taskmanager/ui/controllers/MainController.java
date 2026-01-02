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
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
    private VBox intervalContainer;

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
    private Button createTaskButtonLeft;

    @FXML
    private Button deleteTaskButtonRight;

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

        // ✅ ПУНКТ 1: Фильтр статуса только с NEW и IN_PROGRESS (без CANCELLED и COMPLETED)
        statusFilter.setItems(FXCollections.observableArrayList(
                TaskStatus.NEW,
                TaskStatus.IN_PROGRESS
        ));

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

        dueDateColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            if (task.getDueDate() != null) {
                String formattedDate = task.getDueDate().format(dateFormatter);
                return new javafx.beans.property.SimpleStringProperty(formattedDate);
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        // ✅ ПУНКТ 2: Применить стиль подсвечивания задач на основе категории
        tasksTable.setRowFactory(tableView -> new TableRow<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null) {
                    setStyle("");
                    return;
                }

                // Определяем цвет подсвечивания
                if (task.isOverdue()) {
                    // Просроченные - красное полупрозрачное выделение
                    setStyle("-fx-background-color: rgba(255, 100, 100, 0.15);");
                } else if (task.isTodayOrTomorrow()) {
                    // Сегодня-завтра - жёлтое полупрозрачное выделение
                    setStyle("-fx-background-color: rgba(255, 200, 100, 0.15);");
                } else if (task.isThisWeek()) {
                    // Неделя - голубое полупрозрачное выделение
                    setStyle("-fx-background-color: rgba(100, 150, 255, 0.15);");
                } else {
                    // Нет выделения для остальных
                    setStyle("");
                }
            }
        });

        intervalContainer.setVisible(false);

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

        if (title.isEmpty()) {
            if (description.isEmpty()) {
                showAlert("Ошибка", "Введите описание задачи! Название будет автозаполнено из первой строки.");
                return;
            }
            String[] lines = description.split("\n");
            title = lines[0].trim();
            if (title.isEmpty()) {
                showAlert("Ошибка", "Первая строка описания пуста! Введите название или описание.");
                return;
            }
        }

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

    @FXML
    private void handleDeleteTask() {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите задачу для удаления!");
            return;
        }

        // Показываем диалог подтверждения
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION
        );
        confirmAlert.setTitle("Подтверждение удаления");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Вы уверены, что хотите удалить задачу:\n\"" + selected.getTitle() + "\"?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        // Если пользователь нажал ОК (подтвердил)
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                taskService.deleteTask(selected.getId());
                tasksList.remove(selected);
                showAlert("Успех", "Задача удалена!");
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось удалить задачу: " + e.getMessage());
            }
        }
        // Если нажал Отмена - ничего не делаем
    }

    /**
     * Открыть окно с деталями задачи (редактируемое, большего размера, без кнопки закрыть)
     */
    private void openTaskDetailWindow(Task task) {
        try {
            javafx.stage.Stage detailStage = new javafx.stage.Stage();
            detailStage.setTitle("Задача: " + task.getTitle());
            detailStage.setWidth(700);
            detailStage.setHeight(715);

            javafx.scene.layout.VBox mainVBox = new javafx.scene.layout.VBox(10);
            mainVBox.setStyle("-fx-padding: 15; -fx-font-size: 12;");

            // === ЗАГОЛОВОК (редактируемое) ===
            Label titleLabel = new Label("Название (первая строка описания):");
            titleLabel.setStyle("-fx-font-weight: bold;");
            TextField titleField = new TextField(task.getTitle());
            titleField.setStyle("-fx-font-size: 14; -fx-padding: 5;");

            // === ОПИСАНИЕ (БЕЗ ДУБЛИРОВАНИЯ НАЗВАНИЯ) ===
            Label descLabel = new Label("Остальное описание:");
            descLabel.setStyle("-fx-font-weight: bold;");

            // Берём описание без первой строки (название)
            String fullDescription = task.getDescription();
            String descriptionWithoutTitle = fullDescription;

            int newlineIndex = fullDescription.indexOf('\n');
            if (newlineIndex != -1) {
                descriptionWithoutTitle = fullDescription.substring(newlineIndex + 1);
            } else {
                descriptionWithoutTitle = "";
            }

            TextArea descArea = new TextArea(descriptionWithoutTitle);
            descArea.setWrapText(true);
            descArea.setPrefHeight(120);
            descArea.setStyle("-fx-font-size: 12; -fx-padding: 5;");

            // === СТАТУС ===
            Label statusLabel = new Label("Статус:");
            statusLabel.setStyle("-fx-font-weight: bold;");
            ComboBox<TaskStatus> statusCombo = new ComboBox<>(
                    FXCollections.observableArrayList(TaskStatus.values())
            );
            statusCombo.setValue(task.getStatus());
            statusCombo.setStyle("-fx-padding: 5;");

            // === ПРИОРИТЕТ ===
            Label priorityLabel = new Label("Приоритет (0-10):");
            priorityLabel.setStyle("-fx-font-weight: bold;");
            Spinner<Integer> prioritySpinner2 = new Spinner<>(0, 10, task.getPriority());
            prioritySpinner2.setStyle("-fx-padding: 5;");

            // === ДАТА ВЫПОЛНЕНИЯ ===
            Label dueDateLabel = new Label("Срок выполнения:");
            dueDateLabel.setStyle("-fx-font-weight: bold;");
            DatePicker dueDatePicker2 = new DatePicker(
                    task.getDueDate() != null ? task.getDueDate().toLocalDate() : null
            );
            dueDatePicker2.setStyle("-fx-padding: 5;");

            // === ТИП ПОВТОРА ===
            Label recurrenceLabel = new Label("Тип повтора:");
            recurrenceLabel.setStyle("-fx-font-weight: bold;");
            ComboBox<RecurrenceType> recurrenceCombo2 = new ComboBox<>(
                    FXCollections.observableArrayList(RecurrenceType.values())
            );
            recurrenceCombo2.setValue(task.getRecurrenceType());
            recurrenceCombo2.setStyle("-fx-padding: 5;");

            // === ИНТЕРВАЛ ПОВТОРА ===
            Label intervalLabel = new Label("Интервал повтора (дней):");
            intervalLabel.setStyle("-fx-font-weight: bold;");
            Spinner<Integer> intervalSpinner2 = new Spinner<>(1, 365, task.getRecurrenceInterval());
            intervalSpinner2.setStyle("-fx-padding: 5;");

            // === КНОПКИ (только Сохранить) ===
            javafx.scene.layout.HBox buttonsBox = new javafx.scene.layout.HBox(10);
            buttonsBox.setStyle("-fx-alignment: center;");

            Button saveButton = new Button("💾 Сохранить изменения");
            saveButton.setStyle("-fx-font-size: 12; -fx-padding: 8 16; -fx-font-weight: bold;");
            saveButton.setOnAction(e -> {
                try {
                    // Собираем описание из названия и остального текста
                    String newTitle = titleField.getText().trim();
                    String newDescRest = descArea.getText().trim();

                    if (newTitle.isEmpty()) {
                        showAlert("Ошибка", "Название не может быть пустым!");
                        return;
                    }

                    // Собираем полное описание: название + новая строка + остальное
                    String newFullDescription = newTitle;
                    if (!newDescRest.isEmpty()) {
                        newFullDescription = newTitle + "\n" + newDescRest;
                    }

                    // Обновляем задачу
                    task.setDescription(newFullDescription);
                    task.setStatus(statusCombo.getValue());
                    task.setPriority(prioritySpinner2.getValue());
                    if (dueDatePicker2.getValue() != null) {
                        task.setDueDate(dueDatePicker2.getValue().atStartOfDay());
                    }
                    task.setRecurrenceType(recurrenceCombo2.getValue());
                    task.setRecurrenceInterval(intervalSpinner2.getValue());
                    task.setUpdatedAt(LocalDateTime.now());

                    // Сохраняем в БД
                    taskService.updateTask(task);

                    // Обновляем таблицу
                    tasksTable.refresh();

                    showAlert("Успех", "Задача обновлена!");
                    detailStage.close();
                } catch (Exception ex) {
                    showAlert("Ошибка", "Не удалось сохранить: " + ex.getMessage());
                }
            });

            buttonsBox.getChildren().add(saveButton);

            // === СОБИРАЕМ ВСЁ В VBox ===
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
            javafx.scene.layout.VBox contentVBox = new javafx.scene.layout.VBox(10);
            contentVBox.setStyle("-fx-padding: 10;");

            contentVBox.getChildren().addAll(
                    titleLabel,
                    titleField,
                    new Separator(),
                    descLabel,
                    descArea,
                    new Separator(),
                    statusLabel,
                    statusCombo,
                    priorityLabel,
                    prioritySpinner2,
                    dueDateLabel,
                    dueDatePicker2,
                    recurrenceLabel,
                    recurrenceCombo2,
                    intervalLabel,
                    intervalSpinner2,
                    new Separator(),
                    buttonsBox
            );

            scrollPane.setContent(contentVBox);
            scrollPane.setFitToWidth(true);

            javafx.scene.Scene scene = new javafx.scene.Scene(scrollPane);
            detailStage.setScene(scene);
            detailStage.show();

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось открыть задачу: " + e.getMessage());
        }
    }

    /**
     * Открыть задачу в отдельном окне по двойному клику
     */
    @FXML
    private void handleTaskClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Task selected = tasksTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openTaskDetailWindow(selected);
            }
        }
    }

    /**
     * Обработка изменения типа повтора
     */
    @FXML
    private void handleRecurrenceChange() {
        RecurrenceType selected = recurrenceCombo.getValue();

        if (selected == RecurrenceType.CUSTOM) {
            intervalContainer.setVisible(true);
            intervalSpinner.setDisable(false);
        } else {
            intervalContainer.setVisible(false);
            intervalSpinner.setDisable(true);
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
