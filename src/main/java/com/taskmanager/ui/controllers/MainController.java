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
import javafx.scene.layout.Priority;
import javafx.scene.control.MenuBar;
import java.util.function.Function;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

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

    // ========== ПРИОРИТЕТ ==========
    private static final int PRIORITY_MIN = 0;
    private static final int PRIORITY_MAX = 10;
    private static final int PRIORITY_DEFAULT = 5;


    // ========== ИНТЕРВАЛ РЕКУРСИИ ==========
    private static final int INTERVAL_MIN_DAYS = 1;
    private static final int INTERVAL_MAX_DAYS = 365;
    private static final int INTERVAL_DEFAULT_DAYS = 7;

    // ========== ОБНОВЛЕНИЕ ОПОВЕЩЕНИЙ ==========
    private static final long ALERTS_UPDATE_INTERVAL_SEC = 10;

    // ========== ДАТА И ВРЕМЯ ==========
    private static final int DATE_TIME_MAX_DIGITS = 12;
    private static final int DAY_MIN = 1;
    private static final int DAY_MAX = 31;
    private static final int MONTH_MIN = 1;
    private static final int MONTH_MAX = 12;
    private static final int YEAR_MIN = 1900;
    private static final int YEAR_MAX = 9999;
    private static final int HOUR_MIN = 0;
    private static final int HOUR_MAX = 23;
    private static final int MINUTE_MIN = 0;
    private static final int MINUTE_MAX = 59;

    // ========== UI РАЗМЕРЫ ==========
    private static final double DETAIL_WINDOW_WIDTH = 600;
    private static final double DETAIL_WINDOW_HEIGHT = 650;
    private static final double HOTKEYS_WINDOW_WIDTH = 550;
    private static final double HOTKEYS_WINDOW_HEIGHT = 650;
    private static final double DESCRIPTION_AREA_HEIGHT = 200;

    // ========== ДВОЙНОЙ КЛИК ==========
    private static final int DOUBLE_CLICK_COUNT = 2;

    // ========== ТЕМЫ ==========
    private static final String THEME_DARK = "DARK";
    private static final String THEME_LIGHT = "LIGHT";

    // ========== ФИЛЬТРЫ СТАТУСОВ ==========
    private static final String FILTER_ALL = "ALL";

    // ========== ЦВЕТА (HEX) ==========
    private static final String COLOR_DARK_BG = "#1e1e1e";
    private static final String COLOR_DARK_SURFACE = "#2b2b2b";
    private static final String COLOR_DARK_TEXT = "#ffffff";
    private static final String COLOR_LIGHT_BG = "#f5f5f5";
    private static final String COLOR_LIGHT_SURFACE = "#ffffff";
    private static final String COLOR_LIGHT_TEXT = "#000000";
    private static final String COLOR_LIGHT_BG_ALT = "#f9f9f9";
    private static final String COLOR_LIGHT_TEXT_ALT = "#333333";
    private static final String COLOR_BLUE = "#0000ff";
    private static final String COLOR_YELLOW = "#ffff00";
    private static final String COLOR_DARK_TEXT_SECONDARY = "#e0e0e0";
    private static final String COLOR_DARK_BORDER = "#444444";
    private static final String COLOR_LIGHT_TEXT_SECONDARY = "#333333";
    private static final String COLOR_LIGHT_BORDER = "#cccccc";

    // ========== ПРОЗРАЧНОСТЬ (RGBA) ==========
    private static final String COLOR_OVERDUE = "rgba(255, 100, 100, 0.15)";
    private static final String COLOR_TODAY_TOMORROW = "rgba(255, 200, 100, 0.15)";
    private static final String COLOR_THIS_WEEK = "rgba(100, 150, 255, 0.15)";

    // ==================== ФОРМАТЕРЫ ====================
    private static final DateTimeFormatter tableFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter inputFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // ========== СООБЩЕНИЯ ==========
    private static final String MSG_ERROR = "Ошибка";
    private static final String MSG_SUCCESS = "Успех";
    private static final String MSG_CONFIRMATION = "Подтверждение";
    private static final String MSG_SELECT_TASK = "Выберите задачу для удаления!";
    private static final String MSG_TASK_CREATED = "Задача создана: ";
    private static final String MSG_TASK_DELETED = "Задача удалена!";
    private static final String MSG_TASK_UPDATED = "Задача обновлена!";
    private static final String MSG_INVALID_DATE = "Неверный формат даты: dd.MM.yyyy HH:mm";

    // ========== СТАТУСЫ ДЛЯ ФИЛЬТРА ==========
    private static final String STATUS_NEW = "NEW";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    // ========== ШРИФТЫ ==========
    private static final String FONT_MONOSPACE = "'Consolas', 'Courier New', monospace";
    private static final int FONT_SIZE_DEFAULT = 12;
    private static final int FONT_SIZE_MONOSPACE = 13;
    private static final int FONT_SIZE_TITLE = 22;

    // ========== СТИЛИ UI ==========
    private static final String STYLE_PADDING_10 = "-fx-padding: 10;";
    private static final String STYLE_PADDING_15 = "-fx-padding: 15;";
    private static final String STYLE_PADDING_20 = "-fx-padding: 20;";
    private static final String STYLE_FONT_BOLD = "-fx-font-weight: bold;";
    private static final String STYLE_BUTTON_DEFAULT = "-fx-padding: 10; -fx-font-size: 12;";
    private static final String STYLE_TITLE_LARGE = "-fx-font-size: 22; -fx-font-weight: bold;";
    private static final String STYLE_MONOSPACE_TEXTAREA =
            "-fx-font-family: " + FONT_MONOSPACE + "; -fx-font-size: " + FONT_SIZE_MONOSPACE + ";";

    private static final String STYLE_BUTTON_CLOSE =
            "-fx-padding: 10 30; -fx-font-size: 14; -fx-font-weight: bold;";

    private static final String STYLE_ALIGNMENT_CENTER =
            "-fx-alignment: center;";

    private static final String STYLE_TEXTAREA_DARK =
            STYLE_MONOSPACE_TEXTAREA +
                    " -fx-control-inner-background: " + COLOR_DARK_SURFACE + ";" +
                    " -fx-text-fill: " + COLOR_DARK_TEXT_SECONDARY + ";" +
                    " -fx-border-color: " + COLOR_DARK_BORDER + ";" +
                    " -fx-border-width: 1;";

    private static final String STYLE_TEXTAREA_LIGHT =
            STYLE_MONOSPACE_TEXTAREA +
                    " -fx-control-inner-background: " + COLOR_LIGHT_SURFACE + ";" +
                    " -fx-text-fill: " + COLOR_LIGHT_TEXT_SECONDARY + ";" +
                    " -fx-border-color: " + COLOR_LIGHT_BORDER + ";" +
                    " -fx-border-width: 1;";
    private static final String STYLE_TEXT_FIELD = "-fx-font-size: 14; -fx-padding: 5;";
    private static final String STYLE_TEXT_AREA = "-fx-font-size: 12; -fx-padding: 5;";

    // ========== СТИЛИ ОКОН ==========
    private static final String STYLE_WINDOW_DARK =
            STYLE_PADDING_20 + " -fx-background-color: " + COLOR_DARK_BG + ";";

    private static final String STYLE_WINDOW_LIGHT =
            STYLE_PADDING_20 + " -fx-background-color: " + COLOR_LIGHT_BG_ALT + ";";

    private static final String STYLE_TITLE_DARK =
            STYLE_TITLE_LARGE + " -fx-text-fill: " + COLOR_DARK_TEXT + ";";

    private static final String STYLE_TITLE_LIGHT =
            STYLE_TITLE_LARGE + " -fx-text-fill: " + COLOR_LIGHT_TEXT_ALT + ";";

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
    @FXML private MenuBar menuBar;

    private ObservableList<Task> tasksList;
    private boolean isUpdatingCombo = false;
    private boolean isDarkTheme = false;

    // ==================== ИНИЦИАЛИЗАЦИЯ ====================
    @FXML
    public void initialize() {
        try {
            // Инициализация Spinner для приоритета
            prioritySpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(
                            PRIORITY_MIN, PRIORITY_MAX, PRIORITY_DEFAULT
                    )
            );

            // Инициализация Spinner для интервала повтора
            intervalSpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(
                            INTERVAL_MIN_DAYS, INTERVAL_MAX_DAYS, INTERVAL_DEFAULT_DAYS
                    )
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
                    FILTER_ALL, STATUS_NEW, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_CANCELLED
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
                if (event.getClickCount() == DOUBLE_CLICK_COUNT) {
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
            log.error("Ошибка инициализации интерфейса", e);
            showAlert("Ошибка инициализации", "Ошибка при инициализации интерфейса: " + e.getMessage());
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
            if (THEME_DARK.equals(settings.getTheme())) {
                isDarkTheme = true;
                applyDarkTheme();
            } else {
                isDarkTheme = false;
                applyLightTheme();
            }

            // Применить приоритет по умолчанию
            prioritySpinner.getValueFactory().setValue(settings.getDefaultPriority());

            log.info("Настройки применены: тема={}, приоритет={}",
                    settings.getTheme(), settings.getDefaultPriority());

        } catch (Exception e) {
            log.error("Ошибка загрузки настроек", e);
        }
    }

    /**
     * Применить светлую тему
     */
    private void applyLightTheme() {
        if (rootPane != null) {
            rootPane.setStyle("-fx-base: " + COLOR_LIGHT_SURFACE + "; -fx-background-color: " + COLOR_LIGHT_BG + "; -fx-text-fill: " + COLOR_LIGHT_TEXT + ";");
        }

        tasksTable.setStyle("-fx-background-color: " + COLOR_LIGHT_SURFACE + "; -fx-text-fill: " + COLOR_LIGHT_TEXT + ";");

        if (themeToggleButton != null) {
            themeToggleButton.setText("🌙");
            themeToggleButton.setStyle("-fx-text-fill: " + COLOR_BLUE + ";");
        }
    }

    /**
     * Применить тёмную тему
     */
    private void applyDarkTheme() {
        if (rootPane != null) {
            rootPane.setStyle(String.format(
                    "-fx-base: %s; -fx-background-color: %s; -fx-text-fill: %s;",
                    COLOR_DARK_SURFACE, COLOR_DARK_BG, COLOR_DARK_TEXT
            ));
        }

        tasksTable.setStyle("-fx-background-color: " + COLOR_DARK_SURFACE + "; -fx-text-fill: " + COLOR_DARK_TEXT + ";");

        if (themeToggleButton != null) {
            themeToggleButton.setText("☀");
            themeToggleButton.setStyle("-fx-text-fill: " + COLOR_YELLOW + ";");
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
     * Универсальный метод сортировки по Comparator
     */
    private <T extends Comparable<T>> void sortByComparator(
            Function<Task, T> extractor, boolean ascending) {
        tasksList.sort((t1, t2) -> {
            T v1 = extractor.apply(t1);
            T v2 = extractor.apply(t2);

            // Обработка null значений
            if (v1 == null && v2 == null) return 0;
            if (v1 == null) return ascending ? 1 : -1;
            if (v2 == null) return ascending ? -1 : 1;

            int result = v1.compareTo(v2);
            return ascending ? result : -result;
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
            sortByComparator(Task::getTitle, ascending);
        } else if (sortColumn == statusColumn) {
            sortByComparator(Task::getStatus, ascending);
        } else if (sortColumn == priorityColumn) {
            sortByComparator(Task::getPriority, ascending);
        } else if (sortColumn == dueDateColumn) {
            sortByComparator(Task::getDueDate, ascending);
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
                    setStyle("-fx-background-color: " + COLOR_OVERDUE + ";");
                } else if (task.isTodayOrTomorrow()) {
                    setStyle("-fx-background-color: " + COLOR_TODAY_TOMORROW + ";");
                } else if (task.isThisWeek()) {
                    setStyle("-fx-background-color: " + COLOR_THIS_WEEK + ";");
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

            addEscapeHandlerToAlert(confirmAlert);

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                taskService.deleteTask(selected.getId());
                tasksList.remove(selected);
                showAlert("Успех", "Задача удалена!");
            }

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось удалить задачу: " + e.getMessage());
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
            detailStage.setWidth(DETAIL_WINDOW_WIDTH);
            detailStage.setHeight(DETAIL_WINDOW_HEIGHT);

            VBox mainVBox = new VBox(10);
            mainVBox.setStyle(STYLE_PADDING_15);

            // ПРИМЕНЯЕМ ТЕКУЩУЮ ТЕМУ К ОКНУ
            if (isDarkTheme) {
                mainVBox.setStyle(STYLE_PADDING_15 + " -fx-background-color: " + COLOR_DARK_BG + "; -fx-text-fill: " + COLOR_DARK_TEXT + ";");
            }

            // Название
            Label titleLabel = new Label("Название:");
            titleLabel.setStyle(STYLE_FONT_BOLD);
            TextField titleField = new TextField(task.getTitle());
            titleField.setStyle(STYLE_TEXT_FIELD);

            // Описание (без названия)
            Label descLabel = new Label("Описание:");
            descLabel.setStyle(STYLE_FONT_BOLD);
            TextArea descArea = new TextArea();
            descArea.setWrapText(true);
            descArea.setStyle(STYLE_TEXT_AREA);

            String fullDesc = task.getDescription();
            if (fullDesc != null && fullDesc.contains("\n")) {
                descArea.setText(fullDesc.substring(fullDesc.indexOf("\n") + 1));
            } else if (fullDesc != null) {
                descArea.setText(fullDesc);
            }
            descArea.setPrefHeight(DESCRIPTION_AREA_HEIGHT);

            // Приоритет
            Label priorityLabel = new Label("Приоритет:");
            priorityLabel.setStyle(STYLE_FONT_BOLD);
            Spinner<Integer> prioritySpinner2 = new Spinner<>(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(
                            PRIORITY_MIN, PRIORITY_MAX, task.getPriority()
                    )
            );

            // Дата
            Label dateLabel = new Label("Дата выполнения:");
            dateLabel.setStyle(STYLE_FONT_BOLD);
            TextField dateField = new TextField();
            if (task.getDueDate() != null) {
                dateField.setText(task.getDueDate().format(inputFormatter));
            }

            dateField.textProperty().addListener((obs, oldValue, newValue) -> {
                String digitsOnly = newValue.replaceAll("[^0-9]", "");
                if (digitsOnly.length() > DATE_TIME_MAX_DIGITS) {
                    digitsOnly = digitsOnly.substring(0, DATE_TIME_MAX_DIGITS);
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
            statusLabel.setStyle(STYLE_FONT_BOLD);
            ComboBox<TaskStatus> statusCombo = new ComboBox<>();
            statusCombo.setItems(FXCollections.observableArrayList(TaskStatus.values()));
            statusCombo.setValue(task.getStatus());

            // Кнопки
            Button saveButton = new Button("Сохранить");
            saveButton.setStyle(STYLE_BUTTON_DEFAULT);
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
            cancelButton.setStyle(STYLE_BUTTON_DEFAULT);
            cancelButton.setOnAction(e -> detailStage.close());

            HBox buttonBox = new HBox(10);
            buttonBox.setStyle(STYLE_PADDING_10);
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
                scrollPane.setStyle("-fx-background-color: " + COLOR_DARK_BG + "; -fx-control-inner-background: " + COLOR_DARK_SURFACE + ";");
                scene.setFill(javafx.scene.paint.Color.web(COLOR_DARK_BG));
            }

            detailStage.setScene(scene);
            addEscapeHandler(detailStage);
            detailStage.show();

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось открыть окно редактирования: " + e.getMessage());
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
            if (selectedStatus == null || selectedStatus.equals(FILTER_ALL)) {
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
            log.error("Ошибка при обновлении оповещений", e);
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
            if (digitsOnly.length() > DATE_TIME_MAX_DIGITS) {
                digitsOnly = digitsOnly.substring(0, DATE_TIME_MAX_DIGITS);
            }

            String formatted = formatDateTime(digitsOnly);
            if (!formatted.equals(newValue)) {
                dueDateTimeInput.setText(formatted);
            }
        });

        // ОБРАБОТЧИК НА ПОТЕРЮ ФОКУСА
        dueDateTimeInput.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (wasFocused && !isFocused) {
                autoFillDateTimeForField(dueDateTimeInput);
            }
        });

        // ОБРАБОТЧИК НА ENTER
        dueDateTimeInput.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                autoFillDateTimeForField(dueDateTimeInput);
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
            if (d >= DAY_MIN && d <= DAY_MAX) {
                day = d;
            }
        }

        // Парсим МЕСЯЦ
        if (digitsOnly.length() >= 4) {
            int m = Integer.parseInt(digitsOnly.substring(2, 4));
            if (m >= MONTH_MIN && m <= MONTH_MAX) {
                month = m;
            }
        }

        // Парсим ГОД
        if (digitsOnly.length() >= 8) {
            int y = Integer.parseInt(digitsOnly.substring(4, 8));
            if (y >= YEAR_MIN && y <= YEAR_MAX) {
                year = y;
            }
        }

        // Парсим ЧАСЫ
        if (digitsOnly.length() >= 10) {
            int h = Integer.parseInt(digitsOnly.substring(8, 10));
            if (h >= HOUR_MIN && h <= HOUR_MAX) {
                hour = h;
            }
        }

        // Парсим МИНУТЫ
        if (digitsOnly.length() >= 12) {
            int min = Integer.parseInt(digitsOnly.substring(10, 12));
            if (min >= MINUTE_MIN && min <= MINUTE_MAX) {
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
        intervalSpinner.getValueFactory().setValue(INTERVAL_DEFAULT_DAYS);
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
            if (d >= DAY_MIN && d <= DAY_MAX) {
                day = d;
            }
        }

        if (digitsOnly.length() >= 4) {
            int m = Integer.parseInt(digitsOnly.substring(2, 4));
            if (m >= MONTH_MIN && m <= MONTH_MAX) {
                month = m;
            }
        }

        if (digitsOnly.length() >= 8) {
            int y = Integer.parseInt(digitsOnly.substring(4, 8));
            if (y >= YEAR_MIN && y <= YEAR_MAX) {
                year = y;
            }
        }

        if (digitsOnly.length() >= 10) {
            int h = Integer.parseInt(digitsOnly.substring(8, 10));
            if (h >= HOUR_MIN && h <= HOUR_MAX) {
                hour = h;
            }
        }

        if (digitsOnly.length() >= 12) {
            int min = Integer.parseInt(digitsOnly.substring(10, 12));
            if (min >= MINUTE_MIN && min <= MINUTE_MAX) {
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
     * Фоновое обновление оповещений (JavaFX Timeline)
     */
    private javafx.animation.Timeline alertsTimeline;

    private void startAlertsUpdateThread() {
        alertsTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(ALERTS_UPDATE_INTERVAL_SEC),
                        event -> updateAlertsCount()
                )
        );
        alertsTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        alertsTimeline.play();
        log.info("Timeline запущен для обновления оповещений");
    }

    /**
     * Timeline при закрытииа
     */
    public void shutdown() {
        if (alertsTimeline != null) {
            alertsTimeline.stop();
            log.info("Timeline остановлен");
        }
    }


    private void setupKeyboardShortcuts() {
        // Получаем Scene из rootPane
        if (rootPane == null) {
            log.warn("⚠ rootPane is null, cannot setup keyboard shortcuts");
            return;
        }

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupSceneShortcuts(newScene);
            }
        });

        if (rootPane.getScene() != null) {
            setupSceneShortcuts(rootPane.getScene());
        }
    }

    /**
     * Настройка горячих клавиш для Scene
     */
    private void setupSceneShortcuts(javafx.scene.Scene scene) {
        scene.setOnKeyPressed(event -> {
            // Ctrl + N - Новая задача
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

            // Ctrl + Q - Очистить форму
            else if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.Q) {
                event.consume();
                clearTaskForm();
                showAlert("Форма очищена", "Все поля сброшены (Ctrl+Q)");
            }

            // Escape - Закрыть всплывающее окно
            else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                event.consume();
                handleEscapeKey();
            }

            // F1 - Показать горячие клавиши
            else if (event.getCode() == javafx.scene.input.KeyCode.F1) {
                event.consume();
                handleShowHotkeys();
            }

            // F5 - Обновить список задач
            else if (event.getCode() == javafx.scene.input.KeyCode.F5) {
                event.consume();
                handleRefreshTasks();
            }

            // Ctrl + 0..9 - Быстрая установка приоритета
            else if (event.isControlDown() && event.getCode().isDigitKey()) {
                event.consume();
                int priority = Integer.parseInt(event.getCode().getChar());
                if (priority >= 0 && priority <= 9) {
                    prioritySpinner.getValueFactory().setValue(priority);
                    showAlert("Приоритет изменён", "Установлен приоритет: " + priority);
                }
            }
        });

        log.info("Горячие клавиши настроены");
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

    /**
     * Показать окно с горячими клавишами (меню "Настройки" -> "Горячие клавиши")
     */
    @FXML
    private void handleShowHotkeys() {
        javafx.stage.Stage hotkeysStage = new javafx.stage.Stage();
        hotkeysStage.setTitle("⌨️ Горячие клавиши");
        hotkeysStage.setWidth(550);
        hotkeysStage.setHeight(650);
        hotkeysStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox vbox = new VBox(15);
        vbox.setStyle(STYLE_PADDING_20);

        Label title = new Label("⌨️ Горячие клавиши");
        title.setStyle(STYLE_TITLE_LARGE);

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle(STYLE_MONOSPACE_TEXTAREA);

        String hotkeysText = """
    ╔═══════════════════════════════════════════════════════╗
    ║            ГОРЯЧИЕ КЛАВИШИ ПРИЛОЖЕНИЯ                 ║
    ╚═══════════════════════════════════════════════════════╝
    
    📋 ОСНОВНЫЕ ДЕЙСТВИЯ:
    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    Ctrl + N       │ Новая задача (фокус на поле описания)
    Ctrl + S       │ Сохранить задачу
    Ctrl + D       │ Удалить выбранную задачу
    Delete         │ Удалить выбранную задачу
    Ctrl + E       │ Редактировать выбранную задачу
    Enter          │ Открыть редактирование (при фокусе на таблице)
    
    🎨 ИНТЕРФЕЙС И НАВИГАЦИЯ:
    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    Ctrl + T       │ Сменить тему (светлая ↔ тёмная)
    Ctrl + Q       │ Очистить форму (сбросить все поля)
    Escape         │ Закрыть всплывающее окно (или очистить форму)
    F1             │ Показать это окно горячих клавиш
    F5             │ Обновить список задач из базы данных
    
    ⚡ БЫСТРАЯ УСТАНОВКА ПРИОРИТЕТА:
    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    Ctrl + 0..9    │ Установить приоритет от 0 до 9
    
    💡 ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ:
    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    1️⃣  Создание задачи:
       Ctrl+N → введите описание → Ctrl+S
    
    2️⃣  Редактирование:
       Выберите задачу → Enter → редактируйте → сохраните
    
    3️⃣  Удаление:
       Выберите задачу → Delete → подтвердите удаление
    
    4️⃣  Смена темы:
       Ctrl+T → тема мгновенно изменится
    
    5️⃣  Быстрый приоритет:
       Ctrl+3 → приоритет установлен на 3
    
    6️⃣  Закрытие окон:
       ESC → закрыть текущее всплывающее окно
       Ctrl+Q → очистить форму создания задачи
    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    """;


        textArea.setText(hotkeysText);
        VBox.setVgrow(textArea, javafx.scene.layout.Priority.ALWAYS);

        Button closeButton = new Button("✔ Закрыть");
        closeButton.setOnAction(e -> hotkeysStage.close());
        closeButton.setStyle(STYLE_BUTTON_CLOSE);
        closeButton.setPrefWidth(150);

        HBox buttonBox = new HBox(closeButton);
        buttonBox.setStyle(STYLE_ALIGNMENT_CENTER);

        vbox.getChildren().addAll(title, textArea, buttonBox);

        // Применить текущую тему
        applyThemeToWindow(vbox, title, textArea);

        javafx.scene.Scene scene = new javafx.scene.Scene(vbox);
        hotkeysStage.setScene(scene);
        addEscapeHandler(hotkeysStage);
        hotkeysStage.show();
    }

    /**
     * Применить текущую тему к окну справки
     */
    private void applyThemeToWindow(VBox vbox, Label title, TextArea textArea) {
        if (isDarkTheme) {
            vbox.setStyle(STYLE_WINDOW_DARK);
            title.setStyle(STYLE_TITLE_DARK);
            textArea.setStyle(STYLE_TEXTAREA_DARK);
        } else {
            vbox.setStyle(STYLE_WINDOW_LIGHT);
            title.setStyle(STYLE_TITLE_LIGHT);
            textArea.setStyle(STYLE_TEXTAREA_LIGHT);
        }
    }

    /**
     * Показать окно "О программе"
     */
    @FXML
    private void handleShowAbout() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("Voice Task Manager v1.0");
        alert.setContentText(
                "Менеджер задач с поддержкой голосового ввода\n\n" +
                        "Разработчик: Адам Рубинштейн\n" +
                        "Версия: 1.0.0\n" +
                        "Год: 2026\n\n" +
                        "Технологии:\n" +
                        "• JavaFX 21\n" +
                        "• Spring Boot 3.2\n" +
                        "• H2 Database\n" +
                        "• Telegram Bot API\n\n" +
                        "© 2026 Все права защищены"
        );

        addEscapeHandlerToAlert(alert);
        alert.showAndWait();
    }

    /**
     * Показать руководство пользователя
     */
    @FXML
    private void handleShowUserGuide() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Руководство пользователя");
        alert.setHeaderText("Как использовать приложение");
        alert.setContentText(
                "1. Создание задачи:\n" +
                        "   - Введите описание (обязательно)\n" +
                        "   - Установите приоритет (0-10)\n" +
                        "   - Укажите дату выполнения\n" +
                        "   - Нажмите 'Создать задачу' или Ctrl+S\n\n" +
                        "2. Редактирование:\n" +
                        "   - Дважды щёлкните по задаче\n" +
                        "   - Или выберите задачу и нажмите Enter\n\n" +
                        "3. Удаление:\n" +
                        "   - Выберите задачу → Delete или Ctrl+D\n\n" +
                        "4. Фильтрация:\n" +
                        "   - Используйте выпадающий список 'Фильтр по статусу'\n\n" +
                        "5. Горячие клавиши:\n" +
                        "   - Нажмите F1 для полного списка"
        );

        addEscapeHandlerToAlert(alert);
        alert.showAndWait();
    }

    /**
     * Сообщить об ошибке
     */
    @FXML
    private void handleReportBug() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Сообщить об ошибке");
        alert.setHeaderText("Как сообщить об ошибке");
        alert.setContentText(
                "Если вы нашли ошибку, пожалуйста:\n\n" +
                        "1. Опишите проблему максимально подробно\n" +
                        "2. Укажите шаги для воспроизведения\n" +
                        "3. Приложите скриншот (если возможно)\n\n" +
                        "Отправьте отчёт на:\n" +
                        "🐛 GitHub Issues: github.com/Adam-Rubinstein/task_manager/issues"
        );

        addEscapeHandlerToAlert(alert);
        alert.showAndWait();
    }

    /**
     * Обработка нажатия ESC на главном окне
     */
    private void handleEscapeKey() {
        // Проверяем, есть ли открытые модальные окна
        long modalCount = javafx.stage.Window.getWindows().stream()
                .filter(w -> w instanceof javafx.stage.Stage)
                .map(w -> (javafx.stage.Stage) w)
                .filter(s -> s.getModality() != javafx.stage.Modality.NONE)
                .count();

        if (modalCount == 0) {
            // Если модальных окон нет - очистить форму
            clearTaskForm();
            log.debug("ESC: Форма очищена (нет открытых окон)");
        }
        // Если есть модальные окна - ничего не делаем (они сами обработают ESC)
    }

    /**
     * Добавить обработчик ESC для закрытия окна
     * @param stage Окно, к которому нужно добавить обработчик
     */
    private void addEscapeHandler(javafx.stage.Stage stage) {
        if (stage.getScene() != null) {
            stage.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    event.consume();
                    stage.close();
                    log.debug("ESC: Закрыто окно '{}'", stage.getTitle());
                }
            });
        } else {
            stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.setOnKeyPressed(event -> {
                        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                            event.consume();
                            stage.close();
                            log.debug("ESC: Закрыто окно '{}'", stage.getTitle());
                        }
                    });
                }
            });
        }
    }

    /**
     * Добавить обработчик ESC для Alert
     * @param alert Alert, к которому нужно добавить обработчик
     */
    private void addEscapeHandlerToAlert(javafx.scene.control.Alert alert) {
        alert.getDialogPane().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        event.consume();
                        alert.close();
                        log.debug("ESC: Закрыт Alert '{}'", alert.getTitle());
                    }
                });
            }
        });
    }

}
