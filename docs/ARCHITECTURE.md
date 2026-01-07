🏗️ Архитектура Task Manager v1.5.0
Техническое описание архитектуры JavaFX приложения с Spring Boot и PostgreSQL.

Обзор проекта
Task Manager — настольное приложение для управления задачами, построенное на Java 21 с использованием JavaFX для UI и Spring Boot для бизнес-логики.

Ключевые возможности
✅ CRUD операции с задачами (создание, чтение, обновление, удаление)
✅ Система приоритетов (0-10)
✅ Статусы задач (NEW, IN_PROGRESS, COMPLETED, CANCELLED)
✅ Автозаполнение даты/времени при вводе
✅ Система оповещений с счётчиком непрочитанных
✅ Светлая/тёмная тема интерфейса
✅ Цветовая подсветка задач (просроченные, сегодня, на неделю)
✅ Фильтрация и сортировка задач
✅ Редактирование задач через всплывающее окно

Слоистая архитектура
text
┌──────────────────────────────────────────┐
│ Presentation Layer (UI)                  │
│ ├─ JavaFX UI                             │
│ │  ├─ main-view.fxml                     │
│ │  └─ MainController.java                │
│ └─ Темы оформления (CSS)                 │
└──────────────────┬───────────────────────┘
                   │
┌──────────────────▼───────────────────────┐
│ Service Layer (Business Logic)          │
│ ├─ TaskService                           │
│ │  ├─ createTask()                       │
│ │  ├─ updateTask()                       │
│ │  ├─ deleteTask()                       │
│ │  ├─ getTasksByStatus()                 │
│ │  └─ getAllTasks()                      │
│ ├─ AlertService                          │
│ │  ├─ createAlert()                      │
│ │  ├─ getUnreadAlerts()                  │
│ │  └─ markAsRead()                       │
│ └─ AudioFileService                      │
│    ├─ saveAudioFile()                    │
│    ├─ getAudioFile()                     │
│    └─ deleteExpiredAudio()               │
└──────────────────┬───────────────────────┘
                   │
┌──────────────────▼───────────────────────┐
│ Data Access Layer (DAO/Repository)      │
│ ├─ TaskRepository                        │
│ ├─ AlertRepository                       │
│ └─ AudioFileRepository                   │
└──────────────────┬───────────────────────┘
                   │
┌──────────────────▼───────────────────────┐
│ Persistence Layer                        │
│ └─ PostgreSQL + Hibernate/JPA            │
│    ├─ tasks table                        │
│    ├─ alerts table                       │
│    └─ audio_files table                  │
└──────────────────────────────────────────┘
Модульная структура проекта
text
com.taskmanager/
│
├── TaskManagerApp.java                    # Entry point (@SpringBootApplication)
│
├── config/
│   ├── DatabaseConfig.java                # БД конфигурация
│   └── ThemeManager.java                  # Управление темами (если есть)
│
├── dao/ (Data Access)
│   ├── TaskRepository.java                # JpaRepository<Task, Long>
│   ├── AlertRepository.java               # JpaRepository<Alert, Long>
│   └── AudioFileRepository.java           # JpaRepository<AudioFile, Long>
│
├── model/ (Entity классы)
│   ├── Task.java                          # @Entity задача
│   ├── Alert.java                         # @Entity оповещение
│   ├── AudioFile.java                     # @Entity аудиофайл
│   ├── TaskStatus.java                    # enum (NEW, IN_PROGRESS, ...)
│   ├── AlertType.java                     # enum (NOTIFICATION, REMINDER, ...)
│   └── RecurrenceType.java                # enum (NONE, DAILY, WEEKLY, ...)
│
├── service/ (Business Logic)
│   ├── TaskService.java                   # Бизнес-логика задач
│   ├── AlertService.java                  # Бизнес-логика оповещений
│   └── AudioFileService.java              # Работа с аудиофайлами
│
└── ui/controllers/
    └── MainController.java                # JavaFX контроллер (@Component)
Поток данных
Создание задачи
text
┌─────────────────────────┐
│  User вводит данные     │
│  - Название             │
│  - Описание             │
│  - Приоритет            │
│  - Дата (0812 → auto)   │
│  - Тип повтора          │
└────────────┬────────────┘
             │ нажимает "Создать задачу"
┌────────────▼──────────────────────────┐
│  MainController                       │
│  @FXML handleCreateTask()             │
│  ├─ Валидация полей                   │
│  ├─ Парсинг даты (parseDateTimeInput) │
│  └─ Вызов TaskService                 │
└────────────┬──────────────────────────┘
             │
┌────────────▼──────────────────────────┐
│  TaskService                          │
│  createTask(title, desc, priority,    │
│             dueDate, recurrenceType)  │
│  ├─ Создаёт объект Task              │
│  ├─ Устанавливает status = NEW       │
│  └─ Вызывает TaskRepository.save()   │
└────────────┬──────────────────────────┘
             │
┌────────────▼──────────────────────────┐
│  TaskRepository.save(task)            │
│  └─ Spring Data JPA                   │
└────────────┬──────────────────────────┘
             │ SQL INSERT
┌────────────▼──────────────────────────┐
│  PostgreSQL - tasks table             │
│  INSERT INTO tasks                    │
│    (title, description, due_date,     │
│     priority, status, created_at)     │
│  VALUES (...)                         │
└────────────┬──────────────────────────┘
             │
┌────────────▼──────────────────────────┐
│  MainController                       │
│  ├─ Добавляет задачу в ObservableList │
│  ├─ Таблица обновляется автоматически│
│  └─ Показывает Alert "Успех!"         │
└───────────────────────────────────────┘
Редактирование задачи (двойной клик)
text
┌─────────────────────────┐
│  User делает двойной    │
│  клик на строке задачи  │
└────────────┬────────────┘
             │
┌────────────▼──────────────────────────┐
│  MainController                       │
│  handleDoubleClickTask()              │
│  └─ Вызывает openTaskDetailWindow()  │
└────────────┬──────────────────────────┘
             │
┌────────────▼──────────────────────────┐
│  Создаётся новое окно (Stage)         │
│  ├─ VBox с полями редактирования      │
│  ├─ TextField (название)              │
│  ├─ TextArea (описание)               │
│  ├─ Spinner (приоритет)               │
│  ├─ TextField (дата) + autoFill       │
│  ├─ ComboBox (статус)                 │
│  └─ Кнопки (Сохранить / Отмена)       │
└────────────┬──────────────────────────┘
             │ User нажимает "Сохранить"
┌────────────▼──────────────────────────┐
│  saveButton.setOnAction()             │
│  ├─ Обновляет поля Task объекта       │
│  ├─ Вызывает TaskService.updateTask() │
│  └─ Обновляет строку в таблице        │
└────────────┬──────────────────────────┘
             │
┌────────────▼──────────────────────────┐
│  TaskRepository.save(task)            │
│  └─ SQL UPDATE tasks SET ... WHERE id │
└────────────┬──────────────────────────┘
             │
┌────────────▼──────────────────────────┐
│  Окно закрывается                     │
│  Таблица обновлена                    │
│  Alert "Задача обновлена!"            │
└───────────────────────────────────────┘
Модель данных (Entity классы)
Task (таблица: tasks)
java
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                        // Primary Key
    
    @Column(nullable = false)
    private String title;                   // Название задачи
    
    @Column(columnDefinition = "TEXT")
    private String description;             // Описание
    
    private LocalDateTime dueDate;          // Срок выполнения
    private LocalDateTime createdAt;        // Дата создания
    private LocalDateTime updatedAt;        // Дата обновления
    
    @Enumerated(EnumType.STRING)
    private TaskStatus status;              // NEW, IN_PROGRESS, COMPLETED, CANCELLED
    
    private Integer priority;               // 0-10
    
    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrenceType;  // Тип повтора (резерв)
    
    private Integer recurrenceInterval;     // Интервал повтора (дни)
    
    private Integer version;                // Для оптимистичной блокировки
    
    // Методы для UI (цветовая подсветка)
    public boolean isOverdue() {
        return dueDate != null && 
               dueDate.isBefore(LocalDateTime.now()) && 
               status != TaskStatus.COMPLETED;
    }
    
    public boolean isTodayOrTomorrow() {
        // логика проверки
    }
    
    public boolean isThisWeek() {
        // логика проверки
    }
}
Alert (таблица: alerts)
java
@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;                      // FK → tasks.id
    
    private LocalDateTime alertTime;        // Когда сработать
    
    @Enumerated(EnumType.STRING)
    private AlertType type;                 // NOTIFICATION, REMINDER, DEADLINE
    
    private String message;                 // Текст оповещения
    
    private Boolean isRead = false;         // Прочитано ли
    
    private LocalDateTime createdAt;        // Дата создания
}
AudioFile (таблица: audio_files)
java
@Entity
@Table(name = "audio_files")
public class AudioFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "task_id", unique = true)
    private Task task;                      // FK → tasks.id (UNIQUE)
    
    @Lob
    private byte[] audioData;               // BYTEA (бинарные данные)
    
    private Integer durationSeconds;        // Длительность
    private String fileName;                // Имя файла
    private LocalDateTime createdAt;        // Дата загрузки
    private LocalDateTime expiresAt;        // Дата удаления (30 дней)
}
Сервисный слой
TaskService
Основные методы:

java
@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    // Создать задачу
    public Task createTask(String title, String description, 
                          Integer priority, LocalDateTime dueDate,
                          RecurrenceType recurrenceType) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setStatus(TaskStatus.NEW);
        task.setRecurrenceType(recurrenceType);
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }
    
    // Получить по ID
    public Task getTask(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
    }
    
    // Все задачи
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    // По статусу
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }
    
    // Обновить задачу
    public Task updateTask(Task task) {
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }
    
    // Обновить статус
    public void updateTaskStatus(Long id, TaskStatus status) {
        Task task = getTask(id);
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
    }
    
    // Удалить задачу
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
    
    // Важные задачи (приоритет > 5)
    public List<Task> getImportantTasks() {
        return taskRepository.findByPriorityGreaterThan(5);
    }
    
    // Активные (не завершённые)
    public List<Task> getActiveTasks() {
        return taskRepository.findByStatusNot(TaskStatus.COMPLETED);
    }
}
AlertService
Основные методы:

java
@Service
public class AlertService {
    
    @Autowired
    private AlertRepository alertRepository;
    
    // Создать оповещение
    public Alert createAlert(Task task, LocalDateTime alertTime,
                            AlertType type, String message) {
        Alert alert = new Alert();
        alert.setTask(task);
        alert.setAlertTime(alertTime);
        alert.setType(type);
        alert.setMessage(message);
        alert.setIsRead(false);
        alert.setCreatedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }
    
    // Непрочитанные оповещения
    public List<Alert> getUnreadAlerts() {
        return alertRepository.findByIsReadFalse();
    }
    
    // Отметить как прочитанное
    public void markAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setIsRead(true);
        alertRepository.save(alert);
    }
    
    // Удалить старые оповещения
    public void deleteOldAlerts(LocalDateTime before) {
        alertRepository.deleteByCreatedAtBefore(before);
    }
}
AudioFileService
Основные методы:

java
@Service
public class AudioFileService {
    
    @Autowired
    private AudioFileRepository audioFileRepository;
    
    // Сохранить аудиофайл
    public AudioFile saveAudioFile(Task task, byte[] audioData,
                                   String fileName, Integer duration) {
        AudioFile audioFile = new AudioFile();
        audioFile.setTask(task);
        audioFile.setAudioData(audioData);
        audioFile.setFileName(fileName);
        audioFile.setDurationSeconds(duration);
        audioFile.setCreatedAt(LocalDateTime.now());
        audioFile.setExpiresAt(LocalDateTime.now().plusDays(30));
        return audioFileRepository.save(audioFile);
    }
    
    // Получить по задаче
    public AudioFile getAudioFile(Task task) {
        return audioFileRepository.findByTask(task)
            .orElse(null);
    }
    
    // Удалить истёкшие
    public void deleteExpiredAudio() {
        LocalDateTime now = LocalDateTime.now();
        audioFileRepository.deleteByExpiresAtBefore(now);
    }
}
UI-слой (JavaFX)
main-view.fxml
Структура:

xml
<VBox fx:id="rootPane">
    <!-- Меню бар -->
    <MenuBar>
        <Menu text="Файл">
            <MenuItem text="Выход" onAction="#handleExit"/>
        </Menu>
    </MenuBar>
    
    <HBox>
        <!-- Левая панель: форма создания -->
        <VBox>
            <Label text="СОЗДАНИЕ ЗАДАЧИ"/>
            <ScrollPane>
                <VBox>
                    <Label text="Название:"/>
                    <TextField fx:id="taskNameInput"/>
                    
                    <Label text="Описание:" style="-fx-text-fill: red"/>
                    <TextArea fx:id="taskDescriptionInput"/>
                    
                    <Label text="Приоритет (0-10):"/>
                    <Spinner fx:id="prioritySpinner"/>
                    
                    <Label text="Дата выполнения:"/>
                    <TextField fx:id="dueDateTimeInput"/>
                    
                    <Label text="Тип повтора:"/>
                    <ComboBox fx:id="recurrenceCombo"/>
                    
                    <VBox fx:id="intervalContainer" visible="false">
                        <Label text="Интервал (дней):"/>
                        <Spinner fx:id="intervalSpinner"/>
                    </VBox>
                </VBox>
            </ScrollPane>
            
            <Button fx:id="createTaskButtonLeft" 
                    text="Создать задачу"
                    onAction="#handleCreateTask"/>
        </VBox>
        
        <!-- Правая панель: таблица + оповещения -->
        <VBox>
            <HBox>
                <Button text="Удалить задачу" 
                        onAction="#handleDeleteTask"/>
                <ComboBox fx:id="statusFilter"
                          onAction="#handleFilterByStatus"/>
                <Region HBox.hgrow="ALWAYS"/>
                <Button fx:id="themeToggleButton"
                        onAction="#handleToggleTheme"
                        text="🌙"/>
            </HBox>
            
            <TableView fx:id="tasksTable">
                <columns>
                    <TableColumn fx:id="titleColumn" text="Название"/>
                    <TableColumn fx:id="statusColumn" text="Статус"/>
                    <TableColumn fx:id="priorityColumn" text="Приоритет"/>
                    <TableColumn fx:id="dueDateColumn" text="Срок"/>
                </columns>
            </TableView>
            
            <Label text="ОПОВЕЩЕНИЯ"/>
            <Label fx:id="alertsCountLabel" text="Оповещения: 0"/>
            <ListView fx:id="alertsListView"/>
        </VBox>
    </HBox>
</VBox>
MainController.java
Ключевые особенности:

java
@Component
public class MainController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private AlertService alertService;
    
    @FXML private TableView<Task> tasksTable;
    @FXML private TextField dueDateTimeInput;
    @FXML private Button themeToggleButton;
    @FXML private VBox rootPane;
    
    private ObservableList<Task> tasksList;
    private boolean isDarkTheme = false;
    
    @FXML
    public void initialize() {
        // Настройка компонентов
        setupTableColumns();
        setupTableRowFactory();  // Цветовая подсветка
        setupDateTimeInputMask(); // Автозаполнение даты
        
        // Загрузка данных
        loadTasksByStatuses(TaskStatus.NEW, TaskStatus.IN_PROGRESS);
        updateAlertsCount();
        
        // Фоновое обновление оповещений (каждые 10 сек)
        startAlertsUpdateThread();
    }
    
    // ============ АВТОЗАПОЛНЕНИЕ ДАТЫ ============
    
    private void setupDateTimeInputMask() {
        // Форматирование при вводе
        dueDateTimeInput.textProperty().addListener(...);
        
        // Автозаполнение при потере фокуса
        dueDateTimeInput.focusedProperty().addListener((obs, was, is) -> {
            if (was && !is) autoFillDateTime();
        });
        
        // Автозаполнение по Enter
        dueDateTimeInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                autoFillDateTime();
            }
        });
    }
    
    private void autoFillDateTime() {
        // Парсинг "0812" → "08.12.2026 00:00"
        // С валидацией (31 февраля → текущая дата)
    }
    
    // ============ ЦВЕТОВАЯ ПОДСВЕТКА ============
    
    private void setupTableRowFactory() {
        tasksTable.setRowFactory(tv -> new TableRow<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null || isSelected()) {
                    setStyle("");
                    return;
                }
                
                if (task.isOverdue()) {
                    setStyle("-fx-background-color: rgba(255,100,100,0.15);");
                } else if (task.isTodayOrTomorrow()) {
                    setStyle("-fx-background-color: rgba(255,200,100,0.15);");
                } else if (task.isThisWeek()) {
                    setStyle("-fx-background-color: rgba(100,150,255,0.15);");
                } else {
                    setStyle("");
                }
            }
        });
    }
    
    // ============ СМЕНА ТЕМЫ ============
    
    @FXML
    private void handleToggleTheme() {
        isDarkTheme = !isDarkTheme;
        
        if (isDarkTheme) {
            rootPane.setStyle("-fx-base: #2b2b2b; " +
                            "-fx-background-color: #1e1e1e; " +
                            "-fx-text-fill: #ffffff;");
            tasksTable.setStyle("-fx-background-color: #2b2b2b; " +
                              "-fx-text-fill: #ffffff;");
            themeToggleButton.setText("☀");
        } else {
            rootPane.setStyle("-fx-base: #ffffff; " +
                            "-fx-background-color: #f5f5f5;");
            tasksTable.setStyle("-fx-background-color: #ffffff;");
            themeToggleButton.setText("🌙");
        }
    }
    
    // ============ РЕДАКТИРОВАНИЕ ЗАДАЧИ ============
    
    @FXML
    private void handleDoubleClickTask() {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openTaskDetailWindow(selected);
        }
    }
    
    private void openTaskDetailWindow(Task task) {
        Stage detailStage = new Stage();
        VBox mainVBox = new VBox(10);
        
        // Применение темы к всплывающему окну
        if (isDarkTheme) {
            mainVBox.setStyle("...");
        }
        
        // Поля редактирования с автозаполнением даты
        TextField dateField = new TextField();
        dateField.textProperty().addListener(...);
        dateField.focusedProperty().addListener(...);
        dateField.setOnKeyPressed(...);
        
        // Кнопка "Сохранить"
        saveButton.setOnAction(e -> {
            taskService.updateTask(task);
            tasksList.set(index, task);
            detailStage.close();
        });
    }
}
Интеграция Spring Boot + JavaFX
text
┌─ TaskManagerApp.java (@SpringBootApplication)
│  └─ Запускает Spring контекст
│     ├─ Загружает сервисы (TaskService, AlertService, AudioFileService)
│     ├─ Создаёт репозитории (TaskRepository, AlertRepository, AudioFileRepository)
│     ├─ Инициализирует JavaFX
│     │  └─ FXMLLoader с controllerFactory
│     │     └─ MainController как Spring bean (@Component)
│     └─ Показывает главное окно (Stage)
│
└─ MainController получает Autowired зависимости
   └─ Вызывает TaskService, AlertService через @Autowired
База данных (PostgreSQL)
Схема БД
sql
-- Таблица задач
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    priority INTEGER DEFAULT 5 CHECK (priority BETWEEN 0 AND 10),
    recurrence_type VARCHAR(50),
    recurrence_interval INTEGER,
    version INTEGER DEFAULT 0
);

CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);

-- Таблица оповещений
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    alert_time TIMESTAMP NOT NULL,
    type VARCHAR(50) NOT NULL,
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_alerts_is_read ON alerts(is_read);
CREATE INDEX idx_alerts_time ON alerts(alert_time);

-- Таблица аудиофайлов
CREATE TABLE audio_files (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT UNIQUE NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    audio_data BYTEA NOT NULL,
    duration_seconds INTEGER,
    file_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP
);
Ключевые технические решения
Spring Data JPA — автоматический CRUD через репозитории

Hibernate + ddl-auto=update — автосоздание таблиц при запуске

FXMLLoader + Spring beans — JavaFX контроллеры как компоненты Spring

ObservableList — автообновление таблицы при изменении данных

Platform.runLater() — потокобезопасность для UI обновлений

CSS стили — динамическая смена темы через setStyle()

PostgreSQL индексы — оптимизация запросов по статусу, дате, приоритету

LocalDateTime — работа с датами без временных зон

Enum для статусов — типобезопасность и валидация

Производительность и оптимизация
Индексы БД
sql
-- Ускоряют фильтрацию по статусу
CREATE INDEX idx_tasks_status ON tasks(status);

-- Ускоряют сортировку по дате
CREATE INDEX idx_tasks_due_date ON tasks(due_date);

-- Ускоряют поиск важных задач
CREATE INDEX idx_tasks_priority ON tasks(priority);
Кэширование (будущее улучшение)
java
@Cacheable("tasks")
public List<Task> getAllTasks() {
    return taskRepository.findAll();
}
Batch операции
java
// Вместо N запросов делаем 1
taskRepository.saveAll(tasks);
Безопасность
SQL Injection
✅ Защита: Spring Data JPA использует PreparedStatements автоматически

Оптимистичная блокировка
java
@Version
private Integer version;  // Hibernate автоматически проверяет версию при UPDATE
Планы развития (будущие версии)
Версия 2.0.0
Повторяющиеся задачи (DAILY, WEEKLY, MONTHLY)

Категории и теги

Экспорт/импорт задач (JSON, CSV)

Системные уведомления (Windows/macOS/Linux)

Версия 3.0.0
REST API для мобильного приложения

Web-интерфейс (React)

Синхронизация между устройствами

Версия документа: 1.5.0
Обновлено: 08.01.2026
Статус: ✅ ГОТОВО