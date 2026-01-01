package com.taskmanager.ui.controllers;

import com.taskmanager.service.TaskService;
import com.taskmanager.service.AlertService;
import com.taskmanager.service.AudioFileService;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.Alert;
import com.taskmanager.model.RecurrenceType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class MainController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AudioFileService audioFileService;

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
    private Task currentEditingTask;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @FXML
    public void initialize() {
        prioritySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 5)
        );

        statusFilter.setItems(FXCollections.observableArrayList(TaskStatus.values()));

        recurrenceCombo.setItems(FXCollections.observableArrayList(RecurrenceType.values()));
        recurrenceCombo.setValue(RecurrenceType.ONCE);

        dueDatePicker.setValue(null);

        tasksList = FXCollections.observableArrayList();
        tasksTable.setItems(tasksList);

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

        dueDateColumn.setCellFactory(column -> new TableCell<Task, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");          // вернуть дефолтный стиль
                    return;
                }

                setText(item.format(DATE_FORMATTER));

                // если строка выделена — вообще не лезем в стили, пусть работает стандартный синий фон
                if (getTableRow() != null && getTableRow().isSelected()) {
                    setStyle("");
                    return;
                }

                long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDateTime.now(), item);

                // красим только текст, фон НЕ трогаем
                if (daysUntilDeadline < 0) {
                    setStyle("-fx-text-fill: #8b0000;");       // просрочено
                } else if (daysUntilDeadline <= 1) {
                    setStyle("-fx-text-fill: #8b0000;");       // сегодня/завтра
                } else if (daysUntilDeadline <= 7) {
                    setStyle("-fx-text-fill: #8b6914;");       // в течение недели
                } else {
                    setStyle("");                              // обычный цвет
                }
            }
        });

        tasksTable.setRowFactory(tv -> new TableRow<Task>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getDueDate() == null) {
                    setStyle("");
                    return;
                }

                long daysUntilDeadline =
                        ChronoUnit.DAYS.between(LocalDateTime.now(), item.getDueDate());

                String baseStyle = "";
                if (daysUntilDeadline < 0) {
                    baseStyle = "-fx-background-color: #f5d5d5; -fx-text-fill: #000000;";
                } else if (daysUntilDeadline <= 1) {
                    baseStyle = "-fx-background-color: #ebe0d5; -fx-text-fill: #000000;";
                } else if (daysUntilDeadline <= 7) {
                    baseStyle = "-fx-background-color: #f5f5d5; -fx-text-fill: #000000;";
                }

                // Если строка выбрана, не меняем стиль (оставляем обычный выбор)
                if (this.isSelected()) {
                    setStyle("");
                } else {
                    setStyle(baseStyle);
                }
            }
        });

        loadAllTasks();
        updateAlertsCount();

        tasksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleTaskDoubleClick();
            }
        });

        taskDescriptionInput.setOnKeyPressed(this::handleDescriptionKeyPress);

        Thread alertThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                    Platform.runLater(this::updateAlertsCount);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        alertThread.setDaemon(true);
        alertThread.start();

        Thread updateHighlightThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000);
                    Platform.runLater(() -> tasksTable.refresh());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        updateHighlightThread.setDaemon(true);
        updateHighlightThread.start();
    }

    private void handleDescriptionKeyPress(KeyEvent event) {
        if (event.isShiftDown() && event.getCode() == KeyCode.ENTER) {
            taskDescriptionInput.appendText("\n");
            event.consume();
        }
    }

    private void handleTaskDoubleClick() {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        currentEditingTask = selected;
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Редактирование задачи");
        dialog.setHeaderText(selected.getTitle());

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        TextField titleField = new TextField(selected.getTitle());
        titleField.setStyle("-fx-font-weight: bold;");

        TextArea descArea = new TextArea(selected.getDescription() != null ? selected.getDescription() : "");
        descArea.setWrapText(true);
        descArea.setPrefRowCount(8);
        descArea.setStyle("-fx-control-inner-background: #f5f5f5;");

        ComboBox<TaskStatus> statusCombo = new ComboBox<>(FXCollections.observableArrayList(TaskStatus.values()));
        statusCombo.setValue(selected.getStatus());

        Spinner<Integer> prioritySpinner = new Spinner<>(0, 10, selected.getPriority());

        DatePicker dueDate = new DatePicker(selected.getDueDate() != null ? selected.getDueDate().toLocalDate() : null);

        ComboBox<RecurrenceType> recurrence = new ComboBox<>(FXCollections.observableArrayList(RecurrenceType.values()));
        recurrence.setValue(RecurrenceType.ONCE);

        String formattedCreatedAt = selected.getCreatedAt().format(DATE_TIME_FORMATTER);
        Label createdLabel = new Label("Создана: " + formattedCreatedAt);
        createdLabel.setStyle("-fx-font-size: 11;");

        CheckBox inProgressCheckBox = new CheckBox("Взята в работу");
        inProgressCheckBox.setSelected(selected.getStatus() == TaskStatus.IN_PROGRESS);

        inProgressCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                statusCombo.setValue(TaskStatus.IN_PROGRESS);
            } else {
                if (statusCombo.getValue() == TaskStatus.IN_PROGRESS) {
                    statusCombo.setValue(TaskStatus.NEW);
                }
            }
        });

        statusCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            inProgressCheckBox.setSelected(newVal == TaskStatus.IN_PROGRESS);
        });

        HBox statusButtonsBox = new HBox(10);
        statusButtonsBox.setAlignment(Pos.TOP_RIGHT);

        Button completeBtn = new Button("✓ Выполнено");
        completeBtn.setStyle("-fx-font-size: 12; -fx-padding: 8px 16px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        completeBtn.setOnAction(e -> statusCombo.setValue(TaskStatus.COMPLETED));

        Button cancelBtn = new Button("✗ Отменить");
        cancelBtn.setStyle("-fx-font-size: 12; -fx-padding: 8px 16px; -fx-background-color: #f44336; -fx-text-fill: white;");
        cancelBtn.setOnAction(e -> statusCombo.setValue(TaskStatus.CANCELLED));

        statusButtonsBox.getChildren().addAll(completeBtn, cancelBtn);

        HBox topLineStatusBox = new HBox(20);
        topLineStatusBox.setAlignment(Pos.BASELINE_LEFT);

        VBox statusSection = new VBox(5);
        statusSection.getChildren().addAll(
                new Label("Статус:"),
                statusCombo,
                inProgressCheckBox
        );

        topLineStatusBox.getChildren().addAll(statusSection, statusButtonsBox);
        HBox.setHgrow(statusButtonsBox, javafx.scene.layout.Priority.ALWAYS);

        content.getChildren().addAll(
                new Label("Название:"),
                titleField,
                new Label("Описание:"),
                descArea,
                topLineStatusBox,
                new Label("Приоритет:"),
                prioritySpinner,
                new Label("Дата выполнения:"),
                dueDate,
                new Label("Повтор:"),
                recurrence,
                new Separator(),
                createdLabel
        );

        dialog.getDialogPane().setContent(content);

        ButtonType saveBtn = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelDialog = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, cancelDialog);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveBtn) {
                selected.setTitle(titleField.getText().trim());
                selected.setDescription(descArea.getText().trim());
                selected.setStatus(statusCombo.getValue());
                selected.setPriority(prioritySpinner.getValue());

                LocalDate pickedDate = dueDate.getValue();
                LocalDateTime newDueDate = null;
                if (pickedDate != null) {
                    newDueDate = LocalDateTime.of(pickedDate, LocalTime.of(23, 59, 59));
                }
                selected.setDueDate(newDueDate);

                try {
                    taskService.updateTask(selected);
                    showAlert("Успех", "Задача обновлена!");
                    tasksTable.refresh();
                } catch (Exception e) {
                    showAlert("Ошибка", "Не удалось обновить задачу: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleCreateTask() {
        String title = taskTitleInput.getText().trim();
        String description = taskDescriptionInput.getText().trim();
        Integer priority = prioritySpinner.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        RecurrenceType recurrence = recurrenceCombo.getValue();

        if (title.isEmpty()) {
            if (!description.isEmpty()) {
                String[] lines = description.split("\n");
                title = lines[0].trim();

                if (title.length() > 100) {
                    title = title.substring(0, 100);
                }
            } else {
                showAlert("Ошибка", "Введите название задачи или описание!");
                return;
            }
        }

        try {
            LocalDateTime dueDateTime = null;
            if (dueDate != null) {
                dueDateTime = LocalDateTime.of(dueDate, LocalTime.of(23, 59, 59));
            }

            Task newTask = taskService.createTask(
                    title,
                    description,
                    dueDateTime,
                    priority
            );

            tasksList.add(newTask);
            taskTitleInput.clear();
            taskDescriptionInput.clear();
            prioritySpinner.getValueFactory().setValue(5);
            dueDatePicker.setValue(null);
            recurrenceCombo.setValue(RecurrenceType.ONCE);

            showAlert("Успех", "Задача создана!");
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

        try {
            taskService.deleteTask(selected.getId());
            tasksList.remove(selected);
            showAlert("Успех", "Задача удалена!");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось удалить задачу: " + e.getMessage());
        }
    }

    private void loadAllTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            // Фильтруем: исключаем выполненные и отменённые
            List<Task> filtered = tasks.stream()
                    .filter(task -> task.getStatus() != TaskStatus.COMPLETED &&
                            task.getStatus() != TaskStatus.CANCELLED)
                    .toList();
            tasksList.clear();
            tasksList.addAll(filtered);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось загрузить задачи: " + e.getMessage());
        }
    }

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

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
}
