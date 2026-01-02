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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.time.LocalTime;

@Component
public class MainController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AudioFileService audioFileService;

    // ==================== ФОРМАТЕР ДАТЫ ====================
    private static final DateTimeFormatter tableFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ==================== UI COMPONENTS ====================

    @FXML
    private TextField taskNameInput;

    @FXML
    private TextArea taskDescriptionInput;

    @FXML
    private Spinner<Integer> prioritySpinner;

    @FXML
    private TextField dueDateTimeInput;

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
    private ComboBox<String> statusFilter;

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

        // Инициализация TextField с маской и встроенной кнопкой календаря
        dueDateTimeInput.setText("");
        setupDateTimeInputMask();

        // Фильтр со всеми вариантами
        statusFilter.setItems(FXCollections.observableArrayList(
                "ALL",
                "NEW",
                "IN_PROGRESS",
                "COMPLETED",
                "CANCELLED"
        ));
        statusFilter.setValue("ALL");
        statusFilter.setOnAction(e -> handleFilterByStatus());

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
                String formattedDate = task.getDueDate().format(tableFormatter);
                return new javafx.beans.property.SimpleStringProperty(formattedDate);
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        // ✅ Применить стиль подсвечивания задач на основе категории
        tasksTable.setRowFactory(tableView -> new TableRow<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null) {
                    setStyle("");
                    return;
                }

                // Если строка выделена – оставляем стандартное синее выделение JavaFX
                if (isSelected()) {
                    setStyle("");
                    return;
                }

                // Фоновый цвет только для НЕвыбранных строк
                if (task.isOverdue()) {
                    setStyle("-fx-background-color: rgba(255, 100, 100, 0.15);");
                } else if (task.isTodayOrTomorrow()) {
                    setStyle("-fx-background-color: rgba(255, 200, 100, 0.15);");
                } else if (task.isThisWeek()) {
                    setStyle("-fx-background-color: rgba(100, 150, 255, 0.15);");
                } else {
                    setStyle("");
                }
            }
        });

        intervalContainer.setVisible(false);

        // ✅ Загрузить задачи при запуске (NEW + IN_PROGRESS по умолчанию)
        loadTasksByStatuses(TaskStatus.NEW, TaskStatus.IN_PROGRESS);
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
        RecurrenceType recurrenceType = recurrenceCombo.getValue() != null
                ? recurrenceCombo.getValue()
                : RecurrenceType.NONE;

        // ✅ Парсим дату и время из маскированного поля
        LocalDateTime dueDate;
        String dateTimeStr = dueDateTimeInput.getText().trim();

        if (!dateTimeStr.isEmpty() && dateTimeStr.length() == 16) {
            try {
                dueDate = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            } catch (java.time.format.DateTimeParseException e) {
                showAlert("Ошибка", "Неправильный формат даты! Используйте: dd.MM.yyyy HH:mm\nПример: 03.01.2026 14:30");
                return;
            }
        } else {
            dueDate = null;
        }

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
            dueDateTimeInput.setText("");
            recurrenceCombo.setValue(RecurrenceType.NONE);
            intervalSpinner.getValueFactory().setValue(7);

            showAlert("Успех", "Задача создана!\nНазвание: " + title);

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось создать задачу: " + e.getMessage());
        }
    }

    /**
     * Удалить задачу с подтверждением
     */
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
     * Открыть окно с деталями задачи (редактируемое)
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

            // === ДАТА ВЫПОЛНЕНИЯ СО ВРЕМЕНЕМ (маскированное поле) ===
            Label dueDateLabel = new Label("Срок выполнения (dd.MM.yyyy HH:mm):");
            dueDateLabel.setStyle("-fx-font-weight: bold;");

            TextField dueDateTimeField = new TextField();
            if (task.getDueDate() != null) {
                dueDateTimeField.setText(task.getDueDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            } else {
                dueDateTimeField.setText("");
            }
            dueDateTimeField.setStyle("-fx-padding: 5;");

            // МАСКИРОВАНИЕ ПРИ ВВОДЕ
            dueDateTimeField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue == null || newValue.isEmpty()) {
                    return;
                }

                String digitsOnly = newValue.replaceAll("[^0-9]", "");

                if (digitsOnly.isEmpty()) {
                    return;
                }

                if (digitsOnly.length() > 12) {
                    digitsOnly = digitsOnly.substring(0, 12);
                }

                String formatted = formatDateTime(digitsOnly);

                if (!formatted.equals(newValue)) {
                    dueDateTimeField.setText(formatted);
                }
            });

            // АВТОЗАПОЛНЕНИЕ НА ПОТЕРЮ ФОКУСА
            dueDateTimeField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (wasFocused && !isFocused) {
                    autoFillDateTimeField(dueDateTimeField);
                }
            });

            // АВТОЗАПОЛНЕНИЕ НА ENTER
            dueDateTimeField.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    autoFillDateTimeField(dueDateTimeField);
                }
            });


            // Применяем маску
            dueDateTimeField.textProperty().addListener((obs, oldVal, newVal) -> {
                int caretPosition = dueDateTimeField.getCaretPosition();
                int[] protectedPositions = {2, 5, 10, 13};

                if (newVal.length() < oldVal.length()) {
                    if (caretPosition > 0 && caretPosition <= newVal.length()) {
                        for (int i = 0; i < protectedPositions.length; i++) {
                            if (caretPosition - 1 == protectedPositions[i]) {
                                if (caretPosition - 2 >= 0) {
                                    String before = newVal.substring(0, caretPosition - 2);
                                    String after = newVal.substring(caretPosition - 1);
                                    char protectedChar = oldVal.charAt(caretPosition - 1);
                                    dueDateTimeField.setText(before + protectedChar + after);
                                    dueDateTimeField.positionCaret(caretPosition - 1);
                                    return;
                                }
                            }
                        }
                    }
                }

                if (newVal.length() > 16) {
                    dueDateTimeField.setText(oldVal);
                    dueDateTimeField.positionCaret(caretPosition - 1);
                    return;
                }

                if (!newVal.matches("[0-9. :]*")) {
                    dueDateTimeField.setText(oldVal);
                    dueDateTimeField.positionCaret(caretPosition - 1);
                    return;
                }
            });

            HBox dateTimeBox = new HBox(8);
            dateTimeBox.getChildren().addAll(dueDateTimeField);

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

            // СЛУШАТЕЛЬ ДЛЯ СКРЫТИЯ/ПОКАЗА ИНТЕРВАЛА
            recurrenceCombo2.setOnAction(e -> {
                RecurrenceType selected = recurrenceCombo2.getValue();
                if (selected != null && selected == RecurrenceType.CUSTOM) {
                    intervalLabel.setVisible(true);
                    intervalSpinner2.setVisible(true);
                } else {
                    intervalLabel.setVisible(false);
                    intervalSpinner2.setVisible(false);
                }
            });

            // НАЧАЛЬНОЕ СОСТОЯНИЕ
            if (task.getRecurrenceType() != RecurrenceType.CUSTOM) {
                intervalLabel.setVisible(false);
                intervalSpinner2.setVisible(false);
            }

            // === КНОПКА СОХРАНИТЬ ===
            HBox buttonsBox = new HBox(10);
            buttonsBox.setStyle("-fx-alignment: center;");

            Button saveButton = new Button("💾 Сохранить изменения");
            saveButton.setStyle("-fx-font-size: 12; -fx-padding: 8 16; -fx-font-weight: bold;");
            saveButton.setOnAction(e -> {
                try {
                    String newTitle = titleField.getText().trim();
                    String newDescRest = descArea.getText().trim();

                    if (newTitle.isEmpty()) {
                        showAlert("Ошибка", "Название не может быть пустым!");
                        return;
                    }

                    String newFullDescription = newTitle;
                    if (!newDescRest.isEmpty()) {
                        newFullDescription = newTitle + "\n" + newDescRest;
                    }

                    // ✅ Парсим дату и время
                    LocalDateTime newDueDate = null;
                    String dateTimeStr = dueDateTimeField.getText().trim();
                    if (!dateTimeStr.isEmpty() && dateTimeStr.length() == 16) {
                        try {
                            newDueDate = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                        } catch (java.time.format.DateTimeParseException ex) {
                            showAlert("Ошибка", "Неправильный формат! Используйте: dd.MM.yyyy HH:mm");
                            return;
                        }
                    }

                    task.setDescription(newFullDescription);
                    task.setStatus(statusCombo.getValue());
                    task.setPriority(prioritySpinner2.getValue());
                    if (newDueDate != null) {
                        task.setDueDate(newDueDate);
                    }
                    task.setRecurrenceType(recurrenceCombo2.getValue());
                    task.setRecurrenceInterval(intervalSpinner2.getValue());
                    task.setUpdatedAt(LocalDateTime.now());

                    taskService.updateTask(task);
                    tasksTable.refresh();

                    showAlert("Успех", "Задача обновлена!");
                    detailStage.close();
                } catch (Exception ex) {
                    showAlert("Ошибка", "Не удалось сохранить: " + ex.getMessage());
                }
            });

            buttonsBox.getChildren().add(saveButton);

            // === СОБИРАЕМ ВСЁ В VBox ===
            ScrollPane scrollPane = new ScrollPane();
            VBox contentVBox = new VBox(10);
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
                    dateTimeBox,
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
        String selected = statusFilter.getValue();

        if (selected == null || selected.equals("ALL")) {
            loadTasksByStatuses(TaskStatus.NEW, TaskStatus.IN_PROGRESS);
            return;
        }

        try {
            TaskStatus status = TaskStatus.valueOf(selected);
            List<Task> filtered = taskService.getTasksByStatus(status);
            tasksList.clear();
            tasksList.addAll(filtered);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось отфильтровать задачи: " + e.getMessage());
        }
    }

    /**
     * Загрузить задачи нескольких статусов
     */
    private void loadTasksByStatuses(TaskStatus... statuses) {
        try {
            List<Task> allTasks = new java.util.ArrayList<>();
            for (TaskStatus status : statuses) {
                allTasks.addAll(taskService.getTasksByStatus(status));
            }
            tasksList.clear();
            tasksList.addAll(allTasks);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось загрузить задачи: " + e.getMessage());
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

    /**
     * Форматирование даты с автозаполнением при потере фокуса или Enter
     */
    private void setupDateTimeInputMask() {
        // Слушатель для форматирования при вводе (только точки и двоеточие)
        dueDateTimeInput.textProperty().addListener((obs, oldValue, newValue) -> {
            String digitsOnly = newValue.replaceAll("[^0-9]", "");

            if (digitsOnly.length() > 12) {
                digitsOnly = digitsOnly.substring(0, 12);
            }

            String formatted = formatDateTime(digitsOnly);

            if (!formatted.equals(newValue)) {
                dueDateTimeInput.setText(formatted);
            }
        });

        // Обработчик на потерю фокуса (когда переходишь на другое поле)
        dueDateTimeInput.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (wasFocused && !isFocused) {  // Потеряли фокус
                autoFillDateTime();
            }
        });

        // Обработчик на нажатие Enter
        dueDateTimeInput.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                autoFillDateTime();
            }
        });
    }

    /**
     * Автозаполнение года и времени
     */
    private void autoFillDateTime() {
        String digitsOnly = dueDateTimeInput.getText().replaceAll("[^0-9]", "");

        if (digitsOnly.isEmpty()) {
            return;
        }

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        int currentDay = now.getDayOfMonth();

        int day = currentDay;
        int month = currentMonth;
        int year = currentYear;
        int hour = 0;
        int minute = 0;

        // Парсим ДЕНЬ (позиции 0-1, макс 31)
        if (digitsOnly.length() >= 2) {
            int d = Integer.parseInt(digitsOnly.substring(0, 2));
            if (d >= 1 && d <= 31) {
                day = d;
            }
        } else if (digitsOnly.length() == 1) {
            day = currentDay;
        }

        // Парсим МЕСЯЦ (позиции 2-3, макс 12)
        if (digitsOnly.length() >= 4) {
            int m = Integer.parseInt(digitsOnly.substring(2, 4));
            if (m >= 1 && m <= 12) {
                month = m;
            }
        } else if (digitsOnly.length() >= 3) {
            month = currentMonth;
        }

        // Парсим ГОД (позиции 4-7, должно быть 4 цифры!)
        if (digitsOnly.length() >= 8) {
            int y = Integer.parseInt(digitsOnly.substring(4, 8));
            if (y >= 1900 && y <= 9999) {
                year = y;
            }
        } else if (digitsOnly.length() >= 5) {
            year = currentYear;
        }

        // Парсим ЧАСЫ (позиции 8-9, макс 23)
        if (digitsOnly.length() >= 10) {
            int h = Integer.parseInt(digitsOnly.substring(8, 10));
            if (h >= 0 && h <= 23) {
                hour = h;
            }
        }

        // Парсим МИНУТЫ (позиции 10-11, макс 59)
        if (digitsOnly.length() >= 12) {
            int min = Integer.parseInt(digitsOnly.substring(10, 12));
            if (min >= 0 && min <= 59) {
                minute = min;
            }
        }

        // ПРОВЕРКА ВАЛИДНОСТИ ДАТЫ (существует ли такой день в таком месяце?)
        try {
            LocalDate.of(year, month, day);
            // Если не выбросил исключение, дата валидна
        } catch (java.time.DateTimeException e) {
            // Дата невалидна (например 31 февраля) → берем текущую дату
            day = currentDay;
            month = currentMonth;
            year = currentYear;
        }

        // Форматируем результат
        String formatted = String.format("%02d.%02d.%04d %02d:%02d",
                day, month, year, hour, minute);

        dueDateTimeInput.setText(formatted);
    }

    /**
     * Форматирование 12 цифр в dd.MM.yyyy HH:mm
     */
    private String formatDateTime(String digits) {
        StringBuilder sb = new StringBuilder();

        // День (позиции 0-1)
        if (digits.length() >= 1) sb.append(digits.charAt(0));
        if (digits.length() >= 2) sb.append(digits.charAt(1));

        // Первая точка
        if (digits.length() >= 3) {
            sb.append(".");
            sb.append(digits.charAt(2));
        }
        if (digits.length() >= 4) sb.append(digits.charAt(3));

        // Вторая точка
        if (digits.length() >= 5) {
            sb.append(".");
            sb.append(digits.charAt(4));
        }
        if (digits.length() >= 6) sb.append(digits.charAt(5));
        if (digits.length() >= 7) sb.append(digits.charAt(6));
        if (digits.length() >= 8) sb.append(digits.charAt(7));

        // Пробел
        if (digits.length() >= 9) {
            sb.append(" ");
            sb.append(digits.charAt(8));
        }
        if (digits.length() >= 10) sb.append(digits.charAt(9));

        // Двоеточие
        if (digits.length() >= 11) {
            sb.append(":");
            sb.append(digits.charAt(10));
        }
        if (digits.length() >= 12) sb.append(digits.charAt(11));

        return sb.toString();
    }

    /**
     * Показать popup с календарём под полем
     */
    private void showDatePickerPopup() {
        // Создаём Stage для popup
        javafx.stage.Stage popupStage = new javafx.stage.Stage();
        popupStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        popupStage.setAlwaysOnTop(true);

        // Создаём DatePicker
        DatePicker picker = new DatePicker();

        // Пытаемся распарсить текущую дату
        try {
            String currentText = dueDateTimeInput.getText().trim();
            if (currentText.length() >= 10) {
                String dateStr = currentText.substring(0, 10);
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                picker.setValue(date);
            } else {
                picker.setValue(LocalDate.now());
            }
        } catch (Exception e) {
            picker.setValue(LocalDate.now());
        }

        // Обработчик выбора даты
        picker.setOnAction(e -> {
            LocalDate selectedDate = picker.getValue();
            if (selectedDate != null) {
                String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                dueDateTimeInput.setText(formattedDate + " 00:00");
                popupStage.close();
            }
        });

        // Создаём сцену и показываем popup
        javafx.scene.Scene scene = new javafx.scene.Scene(picker, 350, 320);
        popupStage.setScene(scene);

        // Вычисляем позицию popup (под полем)
        javafx.geometry.Bounds bounds = dueDateTimeInput.localToScreen(dueDateTimeInput.getBoundsInLocal());
        popupStage.setX(bounds.getCenterX() - 175);
        popupStage.setY(bounds.getMaxY() + 5);

        // Закрываем popup при клике вне его
        scene.setOnMouseExited(e -> {
            if (!picker.isShowing()) {
                popupStage.close();
            }
        });

        popupStage.show();
    }

    /**
     * Автозаполнение для поля в окне редактирования задачи
     */
    private void autoFillDateTimeField(TextField field) {
        String digitsOnly = field.getText().replaceAll("[^0-9]", "");

        if (digitsOnly.isEmpty()) {
            return;
        }

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        int currentDay = now.getDayOfMonth();

        int day = currentDay;
        int month = currentMonth;
        int year = currentYear;
        int hour = 0;
        int minute = 0;

        if (digitsOnly.length() >= 2) {
            int d = Integer.parseInt(digitsOnly.substring(0, 2));
            if (d >= 1 && d <= 31) {
                day = d;
            }
        } else if (digitsOnly.length() == 1) {
            day = currentDay;
        }

        if (digitsOnly.length() >= 4) {
            int m = Integer.parseInt(digitsOnly.substring(2, 4));
            if (m >= 1 && m <= 12) {
                month = m;
            }
        } else if (digitsOnly.length() >= 3) {
            month = currentMonth;
        }

        if (digitsOnly.length() >= 8) {
            int y = Integer.parseInt(digitsOnly.substring(4, 8));
            if (y >= 1900 && y <= 9999) {
                year = y;
            }
        } else if (digitsOnly.length() >= 5) {
            year = currentYear;
        }

        if (digitsOnly.length() >= 10) {
            int h = Integer.parseInt(digitsOnly.substring(8, 10));
            if (h >= 0 && h <= 23) {
                hour = h;
            }
        }

        if (digitsOnly.length() >= 12) {
            int min = Integer.parseInt(digitsOnly.substring(10, 12));
            if (min >= 0 && min <= 59) {
                minute = min;
            }
        }

        try {
            LocalDate.of(year, month, day);
        } catch (java.time.DateTimeException e) {
            day = currentDay;
            month = currentMonth;
            year = currentYear;
        }

        String formatted = String.format("%02d.%02d.%04d %02d:%02d",
                day, month, year, hour, minute);

        field.setText(formatted);
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
