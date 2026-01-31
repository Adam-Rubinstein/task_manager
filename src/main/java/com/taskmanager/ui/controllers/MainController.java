package com.taskmanager.ui.controllers;

import com.taskmanager.model.AppSettings;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.AlertService;
import com.taskmanager.service.AudioFileService;
import com.taskmanager.service.SettingsService;
import com.taskmanager.config.ThemeManager;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.RecurrenceType;
import com.taskmanager.model.Alert;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MainController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AudioFileService audioFileService;

    @Autowired
    private SettingsService settingsService;

    @Autowired(required = false)
    private ThemeManager themeManager;

    // ==================== ФОРМАТЕРЫ ====================
    private static final DateTimeFormatter tableFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter inputFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // ==================== UI COMPONENTS ====================

    @FXML private TextField taskNameInput;
    @FXML private TextArea taskDescriptionInput;
    @FXML private Spinner<Integer> prioritySpinner;
    @FXML private TextField dueDateTimeInput;
    @FXML private ComboBox<RecurrenceType> recurrenceCombo;
    @FXML private Spinner<Integer> intervalSpinner;
    @FXML private VBox intervalContainer;
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, TaskStatus> statusColumn;
    @FXML private TableColumn<Task, Integer> priorityColumn;
    @FXML private TableColumn<Task, String> dueDateColumn;
    @FXML private Button createTaskButtonLeft;
    @FXML private Button deleteTaskButtonRight;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label alertsCountLabel;
    @FXML private ListView<String> alertsListView;
    @FXML private Button themeToggleButton;
    @FXML private VBox rootPane;

    private ObservableList<Task> tasksList;
    private boolean isUpdatingCombo = false;
    private boolean isDarkTheme = false;

    // ==================== ИНИЦИАЛИЗАЦИЯ ====================

    @FXML
    public void initialize() {
        try {
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
            recurrenceCombo.setOnAction(e -> handleRecurrenceChange());

            // Инициализация TextField для даты
            dueDateTimeInput.setText("");
            setupDateTimeInputMask();

            // Инициализация фильтра статусов
            statusFilter.setItems(FXCollections.observableArrayList(
                    "ALL", "NEW", "IN_PROGRESS", "COMPLETED", "CANCELLED"
            ));
            statusFilter.setValue("ALL");
            statusFilter.setOnAction(e -> handleFilterByStatus());

            // Инициализация таблицы задач
            tasksList = FXCollections.observableArrayList();
            tasksTable.setItems(tasksList);
            setupTableColumns();
            setupTableRowFactory();

            // Двойной клик на строку таблицы
            tasksTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    handleDoubleClickTask();
                }
            });

            // Инициализация ListView для оповещений
            alertsListView.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    handleMarkAlertAsRead();
                }
            });

            // Загрузить задачи при запуске
            loadTasksByStatuses(TaskStatus.NEW, TaskStatus.IN_PROGRESS);
            updateAlertsCount();

            // Обновлять оповещения каждые 10 секунд
            startAlertsUpdateThread();

            // ЗАГРУЗИТЬ НАСТРОЙКИ ИЗ JSON
            loadSettingsOnStartup();

        } catch (Exception e) {
            showAlert("Ошибка инициализации", "Ошибка при инициализации интерфейса: " + e.getMessage());
            e.printStackTrace();
        }
        setupKeyboardShortcuts();
        setupTooltips();
    }

    // ==================== ЗАГРУЗКА НАСТРОЕК ====================

    /**
     * Загрузить настройки при запуске приложения
     */
    private void loadSettingsOnStartup() {
        try {
            AppSettings settings = settingsService.getCurrentSettings();

            // Применить тему из настроек
            if ("DARK".equals(settings.getTheme())) {
                isDarkTheme = true;
                applyDarkTheme();
            } else {
                isDarkTheme = false;
                applyLightTheme();
            }

            // Применить приоритет по умолчанию
            prioritySpinner.getValueFactory().setValue(settings.getDefaultPriority());

            System.out.println("✅ Настройки применены: тема=" + settings.getTheme() +
                    ", приоритет=" + settings.getDefaultPriority());

        } catch (Exception e) {
            System.err.println("⚠️ Ошибка загрузки настроек: " + e.getMessage());
        }
    }

    /**
     * Применить светлую тему
     */
    private void applyLightTheme() {
        if (rootPane != null) {
            rootPane.setStyle("-fx-base: #ffffff; -fx-background-color: #f5f5f5; -fx-text-fill: #000000;");
        }
        tasksTable.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
        if (themeToggleButton != null) {
            themeToggleButton.setText("🌙");
            themeToggleButton.setStyle("-fx-text-fill: #0000ff;");
        }
    }

    /**
     * Применить тёмную тему
     */
    private void applyDarkTheme() {
        if (rootPane != null) {
            rootPane.setStyle("-fx-base: #2b2b2b; -fx-background-color: #1e1e1e; -fx-text-fill: #ffffff;");
        }
        tasksTable.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: #ffffff;");
        if (themeToggleButton != null) {
            themeToggleButton.setText("☀");
            themeToggleButton.setStyle("-fx-text-fill: #ffff00;");
        }
    }

    // ==================== ИНИЦИАЛИЗАЦИЯ КОМПОНЕНТОВ ====================

    /**
     * Настройка столбцов таблицы
     */
    private void setupTableColumns() {
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

        // Включить сортировку столбцов
        titleColumn.setSortable(true);
        statusColumn.setSortable(true);
        priorityColumn.setSortable(true);
        dueDateColumn.setSortable(true);

        tasksTable.getSortOrder().addListener((javafx.beans.InvalidationListener) obs -> {
            if (!tasksTable.getSortOrder().isEmpty()) {
                sortTasks();
            }
        });
    }

    /**
     * Сортировка таблицы
     */
    private void sortTasks() {
        if (tasksTable.getSortOrder().isEmpty()) {
            return;
        }

        TableColumn<Task, ?> sortColumn = tasksTable.getSortOrder().get(0);
        boolean ascending = sortColumn.getSortType() == TableColumn.SortType.ASCENDING;

        if (sortColumn == titleColumn) {
            tasksList.sort((t1, t2) -> {
                int result = t1.getTitle().compareTo(t2.getTitle());
                return ascending ? result : -result;
            });
        } else if (sortColumn == statusColumn) {
            tasksList.sort((t1, t2) -> {
                int result = t1.getStatus().compareTo(t2.getStatus());
                return ascending ? result : -result;
            });
        } else if (sortColumn == priorityColumn) {
            tasksList.sort((t1, t2) -> {
                int result = Integer.compare(t1.getPriority(), t2.getPriority());
                return ascending ? result : -result;
            });
        } else if (sortColumn == dueDateColumn) {
            tasksList.sort((t1, t2) -> {
                if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                if (t1.getDueDate() == null) return ascending ? 1 : -1;
                if (t2.getDueDate() == null) return ascending ? -1 : 1;
                int result = t1.getDueDate().compareTo(t2.getDueDate());
                return ascending ? result : -result;
            });
        }
    }

    /**
     * Стилизация строк таблицы (подсветка по категориям)
     */
    private void setupTableRowFactory() {
        tasksTable.setRowFactory(tableView -> new TableRow<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setStyle("");
                    return;
                }

                if (isSelected()) {
                    setStyle("");
                    return;
                }

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
    }

    // ==================== ОБРАБОТЧИКИ СОБЫТИЙ ====================

    /**
     * Создать новую задачу
     */
    @FXML
    private void handleCreateTask() {
        try {
            String title = taskNameInput.getText().trim();
            String description = taskDescriptionInput.getText().trim();
            Integer priority = prioritySpinner.getValue();
            RecurrenceType recurrenceType = recurrenceCombo.getValue() != null
                    ? recurrenceCombo.getValue()
                    : RecurrenceType.NONE;

            // Парсим дату и время
            LocalDateTime dueDate = parseDateTimeInput(dueDateTimeInput.getText());

            // Валидация
            if (title.isEmpty() && description.isEmpty()) {
                showAlert("Ошибка", "Введите название или описание задачи!");
                return;
            }

            if (title.isEmpty()) {
                String[] lines = description.split("\\n");
                title = lines[0].trim();
                if (title.isEmpty()) {
                    showAlert("Ошибка", "Первая строка описания пуста!");
                    return;
                }
            }

            if (description.isEmpty()) {
                description = title;
            }

            // Создание задачи
            Task newTask = taskService.createTask(title, description, priority, dueDate, recurrenceType);
            tasksList.add(newTask);

            // Очистка формы
            clearTaskForm();
            showAlert("Успех", "Задача создана: " + title);

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось создать задачу: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Удалить выбранную задачу
     */
    @FXML
    private void handleDeleteTask() {
        try {
            Task selected = tasksTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Ошибка", "Выберите задачу для удаления!");
                return;
            }

            javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Подтверждение");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Удалить задачу: \"" + selected.getTitle() + "\"?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                taskService.deleteTask(selected.getId());
                tasksList.remove(selected);
                showAlert("Успех", "Задача удалена!");
            }

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось удалить задачу: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Двойной клик на задачу = открыть редактирование
     */
    @FXML
    private void handleDoubleClickTask() {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openTaskDetailWindow(selected);
        }
    }

    /**
     * Окно редактирования задачи
     */
    private void openTaskDetailWindow(Task task) {
        try {
            javafx.stage.Stage detailStage = new javafx.stage.Stage();
            detailStage.setTitle("Редактирование: " + task.getTitle());
            detailStage.setWidth(600);
            detailStage.setHeight(650);

            VBox mainVBox = new VBox(10);
            mainVBox.setStyle("-fx-padding: 15;");

            // ПРИМЕНЯЕМ ТЕКУЩУЮ ТЕМУ К ОКНУ
            if (isDarkTheme) {
                mainVBox.setStyle("-fx-padding: 15; -fx-background-color: #1e1e1e; -fx-text-fill: #ffffff;");
            }

            // Название
            Label titleLabel = new Label("Название:");
            titleLabel.setStyle("-fx-font-weight: bold;");
            TextField titleField = new TextField(task.getTitle());
            titleField.setStyle("-fx-font-size: 14; -fx-padding: 5;");

            // Описание (без названия)
            Label descLabel = new Label("Описание:");
            descLabel.setStyle("-fx-font-weight: bold;");
            TextArea descArea = new TextArea();
            descArea.setWrapText(true);
            descArea.setStyle("-fx-font-size: 12; -fx-padding: 5;");

            String fullDesc = task.getDescription();
            if (fullDesc != null && fullDesc.contains("\n")) {
                descArea.setText(fullDesc.substring(fullDesc.indexOf("\n") + 1));
            } else if (fullDesc != null) {
                descArea.setText(fullDesc);
            }
            descArea.setPrefHeight(200);

            // Приоритет
            Label priorityLabel = new Label("Приоритет:");
            priorityLabel.setStyle("-fx-font-weight: bold;");
            Spinner<Integer> prioritySpinner2 = new Spinner<>(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, task.getPriority())
            );

            // Дата
            Label dateLabel = new Label("Дата выполнения:");
            dateLabel.setStyle("-fx-font-weight: bold;");
            TextField dateField = new TextField();
            if (task.getDueDate() != null) {
                dateField.setText(task.getDueDate().format(inputFormatter));
            }

            dateField.textProperty().addListener((obs, oldValue, newValue) -> {
                String digitsOnly = newValue.replaceAll("[^0-9]", "");
                if (digitsOnly.length() > 12) {
                    digitsOnly = digitsOnly.substring(0, 12);
                }
                String formatted = formatDateTime(digitsOnly);
                if (!formatted.equals(newValue)) {
                    dateField.setText(formatted);
                }
            });

            dateField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (wasFocused && !isFocused) {
                    autoFillDateTimeForField(dateField);
                }
            });

            dateField.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    autoFillDateTimeForField(dateField);
                }
            });

            // Статус
            Label statusLabel = new Label("Статус:");
            statusLabel.setStyle("-fx-font-weight: bold;");
            ComboBox<TaskStatus> statusCombo = new ComboBox<>();
            statusCombo.setItems(FXCollections.observableArrayList(TaskStatus.values()));
            statusCombo.setValue(task.getStatus());

            // Кнопки
            Button saveButton = new Button("Сохранить");
            saveButton.setStyle("-fx-padding: 10; -fx-font-size: 12;");
            saveButton.setOnAction(e -> {
                try {
                    task.setDescription(titleField.getText() + "\n" + descArea.getText());
                    task.setPriority(prioritySpinner2.getValue());
                    task.setStatus(statusCombo.getValue());

                    String dateStr = dateField.getText().trim();
                    if (!dateStr.isEmpty()) {
                        try {
                            task.setDueDate(LocalDateTime.parse(dateStr, inputFormatter));
                        } catch (Exception ex) {
                            showAlert("Ошибка", "Неверный формат даты: dd.MM.yyyy HH:mm");
                            return;
                        }
                    }

                    taskService.updateTask(task);

                    // Обновить строку в таблице
                    int index = tasksList.indexOf(task);
                    if (index >= 0) {
                        tasksList.set(index, task);
                    }

                    detailStage.close();
                    showAlert("Успех", "Задача обновлена!");
                } catch (Exception ex) {
                    showAlert("Ошибка", "Не удалось сохранить: " + ex.getMessage());
                }
            });

            Button cancelButton = new Button("Отмена");
            cancelButton.setStyle("-fx-padding: 10; -fx-font-size: 12;");
            cancelButton.setOnAction(e -> detailStage.close());

            HBox buttonBox = new HBox(10);
            buttonBox.setStyle("-fx-padding: 10;");
            buttonBox.getChildren().addAll(saveButton, cancelButton);

            // Добавить все элементы в форму
            mainVBox.getChildren().addAll(
                    titleLabel, titleField,
                    descLabel, descArea,
                    priorityLabel, prioritySpinner2,
                    statusLabel, statusCombo,
                    dateLabel, dateField,
                    buttonBox
            );

            ScrollPane scrollPane = new ScrollPane(mainVBox);
            scrollPane.setFitToWidth(true);
            javafx.scene.Scene scene = new javafx.scene.Scene(scrollPane);

            // ПРИМЕНЯЕМ ТЕМУ К SCROLL PANE И СЦЕНЕ
            if (isDarkTheme) {
                scrollPane.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #2b2b2b;");
                scene.setFill(javafx.scene.paint.Color.web("#1e1e1e"));
            }

            detailStage.setScene(scene);
            detailStage.show();

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось открыть окно редактирования: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Обработчик изменения типа рекурсии
     */
    @FXML
    private void handleRecurrenceChange() {
        if (isUpdatingCombo) return;

        RecurrenceType selected = recurrenceCombo.getValue();
        if (selected == null) {
            selected = RecurrenceType.NONE;
            recurrenceCombo.setValue(selected);
        }

        if (intervalContainer != null) {
            intervalContainer.setVisible(selected == RecurrenceType.CUSTOM);
        }
    }

    /**
     * Фильтр по статусу
     */
    @FXML
    private void handleFilterByStatus() {
        try {
            String selectedStatus = statusFilter.getValue();
            if (selectedStatus == null || selectedStatus.equals("ALL")) {
                loadTasksByStatuses(TaskStatus.NEW, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED, TaskStatus.CANCELLED);
            } else {
                TaskStatus status = TaskStatus.valueOf(selectedStatus);
                loadTasksByStatuses(status);
            }
        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка фильтрации: " + e.getMessage());
        }
    }

    /**
     * Отметить оповещение как прочитанное
     */
    @FXML
    private void handleMarkAlertAsRead() {
        try {
            int selectedIndex = alertsListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                List<Alert> unread = alertService.getUnreadAlerts();
                if (selectedIndex < unread.size()) {
                    Alert alert = unread.get(selectedIndex);
                    alertService.markAsRead(alert.getId());
                    updateAlertsCount();
                    showAlert("Успех", "Оповещение отмечено как прочитанное");
                }
            } else {
                showAlert("Ошибка", "Выберите оповещение!");
            }
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось отметить оповещение: " + e.getMessage());
        }
    }

    /**
     * Переключить тему (с автосохранением в JSON)
     */
    @FXML
    private void handleToggleTheme() {
        try {
            isDarkTheme = !isDarkTheme;

            if (isDarkTheme) {
                applyDarkTheme();
                settingsService.updateTheme("DARK");
            } else {
                applyLightTheme();
                settingsService.updateTheme("LIGHT");
            }

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось переключить тему: " + e.getMessage());
        }
    }

    /**
     * Выход
     */
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Загрузить задачи по статусам
     */
    private void loadTasksByStatuses(TaskStatus... statuses) {
        try {
            if (statuses == null || statuses.length == 0) {
                tasksList.clear();
                return;
            }

            List<Task> allTasks = new java.util.ArrayList<>();
            for (TaskStatus status : statuses) {
                if (status != null) {
                    allTasks.addAll(taskService.getTasksByStatus(status));
                }
            }

            tasksList.clear();
            tasksList.addAll(allTasks);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось загрузить задачи: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Обновить счетчик оповещений и список
     */
    private void updateAlertsCount() {
        try {
            List<Alert> unread = alertService.getUnreadAlerts();

            if (alertsCountLabel != null) {
                alertsCountLabel.setText(String.valueOf(unread.size()));
            }

            if (alertsListView != null) {
                ObservableList<String> alertItems = FXCollections.observableArrayList(
                        unread.stream()
                                .map(alert -> String.format("[%s] %s",
                                        alert.getType(),
                                        alert.getMessage() != null ? alert.getMessage() : "No message"))
                                .collect(Collectors.toList())
                );
                alertsListView.setItems(alertItems);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении оповещений: " + e.getMessage());
        }
    }

    /**
     * Парсинг даты из текстового поля
     */
    private LocalDateTime parseDateTimeInput(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateStr.trim(), inputFormatter);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Настройка маски ввода даты
     */
    private void setupDateTimeInputMask() {
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

        // ОБРАБОТЧИК НА ПОТЕРЮ ФОКУСА
        dueDateTimeInput.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (wasFocused && !isFocused) {
                autoFillDateTime();
            }
        });

        // ОБРАБОТЧИК НА ENTER
        dueDateTimeInput.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                autoFillDateTime();
            }
        });
    }

    /**
     * АВТОЗАПОЛНЕНИЕ ГОДА И ВРЕМЕНИ
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

        // Парсим ДЕНЬ
        if (digitsOnly.length() >= 2) {
            int d = Integer.parseInt(digitsOnly.substring(0, 2));
            if (d >= 1 && d <= 31) {
                day = d;
            }
        }

        // Парсим МЕСЯЦ
        if (digitsOnly.length() >= 4) {
            int m = Integer.parseInt(digitsOnly.substring(2, 4));
            if (m >= 1 && m <= 12) {
                month = m;
            }
        }

        // Парсим ГОД
        if (digitsOnly.length() >= 8) {
            int y = Integer.parseInt(digitsOnly.substring(4, 8));
            if (y >= 1900 && y <= 9999) {
                year = y;
            }
        }

        // Парсим ЧАСЫ
        if (digitsOnly.length() >= 10) {
            int h = Integer.parseInt(digitsOnly.substring(8, 10));
            if (h >= 0 && h <= 23) {
                hour = h;
            }
        }

        // Парсим МИНУТЫ
        if (digitsOnly.length() >= 12) {
            int min = Integer.parseInt(digitsOnly.substring(10, 12));
            if (min >= 0 && min <= 59) {
                minute = min;
            }
        }

        // ПРОВЕРКА ВАЛИДНОСТИ ДАТЫ
        try {
            LocalDate.of(year, month, day);
        } catch (java.time.DateTimeException e) {
            showAlert("Ошибка", "Неверная дата: " + day + "." + month + "." + year);
            day = currentDay;
            month = currentMonth;
            year = currentYear;
        }

        String formatted = String.format("%02d.%02d.%04d %02d:%02d",
                day, month, year, hour, minute);
        dueDateTimeInput.setText(formatted);
    }

    /**
     * Форматирование даты из цифр
     */
    private String formatDateTime(String digits) {
        StringBuilder sb = new StringBuilder();

        if (digits.length() >= 1) sb.append(digits.charAt(0));
        if (digits.length() >= 2) sb.append(digits.charAt(1));
        if (digits.length() >= 3) {
            sb.append(".");
            sb.append(digits.charAt(2));
        }
        if (digits.length() >= 4) sb.append(digits.charAt(3));
        if (digits.length() >= 5) {
            sb.append(".");
            sb.append(digits.substring(4, Math.min(8, digits.length())));
        }
        if (digits.length() >= 9) {
            sb.append(" ");
            sb.append(digits.charAt(8));
        }
        if (digits.length() >= 10) sb.append(digits.charAt(9));
        if (digits.length() >= 11) {
            sb.append(":");
            sb.append(digits.charAt(10));
        }
        if (digits.length() >= 12) sb.append(digits.charAt(11));

        return sb.toString();
    }

    /**
     * Очистить форму создания задачи
     */
    private void clearTaskForm() {
        taskNameInput.clear();
        taskDescriptionInput.clear();

        // Получить приоритет по умолчанию из настроек
        AppSettings settings = settingsService.getCurrentSettings();
        prioritySpinner.getValueFactory().setValue(settings.getDefaultPriority());

        dueDateTimeInput.clear();
        recurrenceCombo.setValue(RecurrenceType.NONE);
        intervalSpinner.getValueFactory().setValue(7);
        if (intervalContainer != null) {
            intervalContainer.setVisible(false);
        }
    }

    /**
     * Показать алерт (использует JavaFX Alert)
     */
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * АВТОЗАПОЛНЕНИЕ ДЛЯ ЛЮБОГО TextField (универсальный)
     */
    private void autoFillDateTimeForField(TextField field) {
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
        }

        if (digitsOnly.length() >= 4) {
            int m = Integer.parseInt(digitsOnly.substring(2, 4));
            if (m >= 1 && m <= 12) {
                month = m;
            }
        }

        if (digitsOnly.length() >= 8) {
            int y = Integer.parseInt(digitsOnly.substring(4, 8));
            if (y >= 1900 && y <= 9999) {
                year = y;
            }
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

    /**
     * Фоновый поток для обновления оповещений
     */
    private void startAlertsUpdateThread() {
        Thread alertThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000); // 10 секунд
                    Platform.runLater(this::updateAlertsCount);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        alertThread.setDaemon(true);
        alertThread.start();
    }

    private void setupKeyboardShortcuts() {
        // Получаем Scene из rootPane
        if (rootPane == null) {
            System.err.println("⚠️ rootPane is null, cannot setup keyboard shortcuts");
            return;
        }

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupSceneShortcuts(newScene);
            }
        });

        // Если Scene уже есть, настроить сразу
        if (rootPane.getScene() != null) {
            setupSceneShortcuts(rootPane.getScene());
        }
    }

    /**
     * Настройка горячих клавиш для Scene
     */
    private void setupSceneShortcuts(javafx.scene.Scene scene) {
        scene.setOnKeyPressed(event -> {
            // Ctrl + N - Новая задача (фокус на описание)
            if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.N) {
                event.consume();
                taskDescriptionInput.requestFocus();
                showAlert("Горячая клавиша", "Ctrl+N: Создание новой задачи");
            }

            // Ctrl + S - Сохранить задачу
            else if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.S) {
                event.consume();
                handleCreateTask();
            }

            // Ctrl + D или Delete - Удалить задачу
            else if ((event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.D) ||
                    event.getCode() == javafx.scene.input.KeyCode.DELETE) {
                event.consume();
                handleDeleteTask();
            }

            // Ctrl + E или Enter - Редактировать задачу
            else if ((event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.E) ||
                    (!event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.ENTER &&
                            tasksTable.isFocused())) {
                event.consume();
                handleDoubleClickTask();
            }

            // Ctrl + T - Сменить тему
            else if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.T) {
                event.consume();
                handleToggleTheme();
            }

            // Ctrl + Q - Выход
            else if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.Q) {
                event.consume();
                handleExit();
            }

            // Escape - Очистить форму
            else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                event.consume();
                clearTaskForm();
                showAlert("Форма очищена", "Все поля сброшены");
            }

            // F5 - Обновить список задач
            else if (event.getCode() == javafx.scene.input.KeyCode.F5) {
                event.consume();
                handleRefreshTasks();
            }

            // Ctrl + 1..5 - Быстрая установка приоритета
            else if (event.isControlDown() && event.getCode().isDigitKey()) {
                event.consume();
                int priority = Integer.parseInt(event.getCode().getChar());
                if (priority >= 0 && priority <= 9) {
                    prioritySpinner.getValueFactory().setValue(priority);
                    showAlert("Приоритет изменён", "Установлен приоритет: " + priority);
                }
            }
        });

        System.out.println("✅ Горячие клавиши настроены");
    }

    /**
     * Обновить список задач (горячая клавиша F5)
     */
    private void handleRefreshTasks() {
        try {
            String currentFilter = statusFilter.getValue();
            if (currentFilter == null || currentFilter.equals("ALL")) {
                loadTasksByStatuses(TaskStatus.NEW, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED, TaskStatus.CANCELLED);
            } else {
                TaskStatus status = TaskStatus.valueOf(currentFilter);
                loadTasksByStatuses(status);
            }
            showAlert("Обновлено", "Список задач обновлён из базы данных");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось обновить задачи: " + e.getMessage());
        }
    }

    /**
     * Настройка подсказок (Tooltip) для элементов UI
     */
    private void setupTooltips() {
        // Кнопка создания задачи
        if (createTaskButtonLeft != null) {
            Tooltip createTooltip = new Tooltip("Создать задачу (Ctrl+S)");
            createTaskButtonLeft.setTooltip(createTooltip);
        }

        // Кнопка удаления задачи
        if (deleteTaskButtonRight != null) {
            Tooltip deleteTooltip = new Tooltip("Удалить задачу (Ctrl+D или Delete)");
            deleteTaskButtonRight.setTooltip(deleteTooltip);
        }

        // Кнопка смены темы
        if (themeToggleButton != null) {
            Tooltip themeTooltip = new Tooltip("Сменить тему (Ctrl+T)");
            themeToggleButton.setTooltip(themeTooltip);
        }

        // Таблица задач
        if (tasksTable != null) {
            Tooltip tableTooltip = new Tooltip("Enter - редактировать, Delete - удалить, F5 - обновить");
            Tooltip.install(tasksTable, tableTooltip);
        }
    }


}
