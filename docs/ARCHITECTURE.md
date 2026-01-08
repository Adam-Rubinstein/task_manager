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
Приложение построено на основе четырёхслойной архитектуры:

1. Presentation Layer (UI)
JavaFX UI - графический интерфейс пользователя

main-view.fxml - разметка интерфейса в XML формате

MainController.java - логика взаимодействия с UI компонентами

Темы оформления - CSS стили

Светлая тема (по умолчанию)

Тёмная тема (переключение кнопкой)

2. Service Layer (Business Logic)
TaskService - управление задачами

createTask() - создание новой задачи

updateTask() - изменение существующей задачи

deleteTask() - удаление задачи

getTasksByStatus() - поиск по статусу

getAllTasks() - получить все задачи

AlertService - управление оповещениями

createAlert() - создание оповещения

getUnreadAlerts() - получить непрочитанные

markAsRead() - отметить как прочитанное

AudioFileService - работа с аудиофайлами

saveAudioFile() - сохранить аудиозапись

getAudioFile() - получить аудио по задаче

deleteExpiredAudio() - удалить старые аудио

3. Data Access Layer (DAO/Repository)
TaskRepository - Spring Data JPA для работы с задачами
AlertRepository - Spring Data JPA для оповещений
AudioFileRepository - Spring Data JPA для аудиофайлов

4. Persistence Layer
PostgreSQL - реляционная база данных
Hibernate/JPA - маппинг объектов на таблицы

Таблица tasks - все задачи

Таблица alerts - все оповещения

Таблица audio_files - все аудиофайлы

Модульная структура проекта
text
com.taskmanager/
│
├─── TaskManagerApp.java
│    Точка входа приложения (@SpringBootApplication)
│    Инициализирует Spring контекст и JavaFX окно
│
├─── config/
│    │
│    ├─── DatabaseConfig.java
│    │    Конфигурация подключения к PostgreSQL
│    │    Настройка пула соединений HikariCP
│    │
│    └─── ThemeManager.java
│         Управление темами оформления (опционально)
│
├─── dao/
│    │
│    ├─── TaskRepository.java
│    │    Spring Data JPA репозиторий для Task
│    │    Наследует JpaRepository<Task, Long>
│    │    Автоматический CRUD без SQL
│    │
│    ├─── AlertRepository.java
│    │    Spring Data JPA репозиторий для Alert
│    │    Методы для поиска непрочитанных оповещений
│    │
│    └─── AudioFileRepository.java
│         Spring Data JPA репозиторий для AudioFile
│         Методы для работы с аудиофайлами
│
├─── model/
│    │
│    ├─── Task.java
│    │    @Entity - сущность задачи
│    │    Поля: id, title, description, dueDate, status, priority
│    │    Методы: isOverdue(), isTodayOrTomorrow(), isThisWeek()
│    │
│    ├─── Alert.java
│    │    @Entity - сущность оповещения
│    │    Поля: id, taskId, alertTime, type, message, isRead
│    │    Связь: Many-to-One с Task
│    │
│    ├─── AudioFile.java
│    │    @Entity - сущность аудиофайла
│    │    Поля: id, taskId, audioData, durationSeconds, fileName
│    │    Связь: One-to-One с Task
│    │
│    ├─── TaskStatus.java
│    │    Enum для статусов задачи
│    │    Значения: NEW, IN_PROGRESS, COMPLETED, CANCELLED
│    │
│    ├─── AlertType.java
│    │    Enum для типов оповещений
│    │    Значения: NOTIFICATION, REMINDER, DEADLINE
│    │
│    └─── RecurrenceType.java
│         Enum для типов повторения задач
│         Значения: NONE, DAILY, WEEKLY, MONTHLY, CUSTOM
│
├─── service/
│    │
│    ├─── TaskService.java
│    │    Бизнес-логика работы с задачами
│    │    Использует TaskRepository для сохранения
│    │    Аннотация @Service
│    │
│    ├─── AlertService.java
│    │    Бизнес-логика работы с оповещениями
│    │    Использует AlertRepository для сохранения
│    │    Аннотация @Service
│    │
│    └─── AudioFileService.java
│         Бизнес-логика работы с аудиофайлами
│         Использует AudioFileRepository для сохранения
│         Аннотация @Service
│
└─── ui/
     │
     └─── controllers/
          │
          └─── MainController.java
               JavaFX контроллер (@Component)
               Связывает UI с сервисным слоем
               Обрабатывает все действия пользователя
               
               Основные методы:
               - initialize() - инициализация UI
               - handleCreateTask() - создание задачи
               - handleDeleteTask() - удаление задачи
               - handleToggleTheme() - смена темы
               - autoFillDateTime() - автозаполнение даты
               - openTaskDetailWindow() - редактирование задачи
Поток данных
Сценарий 1: Создание новой задачи
Этап 1 - Ввод данных

Пользователь заполняет форму на левой панели:

Название (опционально)

Описание (обязательно)

Приоритет (0-10)

Дата выполнения (автозаполнение: 0812 → 08.12.2026 00:00)

Тип повтора

Нажимает кнопку "Создать задачу"

Этап 2 - Обработка в контроллере

Вызывается метод MainController.handleCreateTask()

Валидация полей ввода

Парсинг даты через autoFillDateTime()

Вызов TaskService.createTask()

Этап 3 - Сервис

Сервис TaskService создаёт объект Task

Устанавливает статус = NEW

Устанавливает время создания

Вызывает TaskRepository.save()

Этап 4 - Сохранение в БД

Repository выполняет SQL INSERT в таблицу tasks

PostgreSQL вставляет строку в базу

Возвращает созданный объект с присвоенным ID

Этап 5 - Обновление UI

Контроллер добавляет задачу в ObservableList

TableView автоматически обновляется

Пользователь видит новую задачу в таблице

Показывается Alert "Задача успешно создана"

Сценарий 2: Редактирование задачи (двойной клик)
Этап 1 - Выбор задачи

Пользователь делает двойной клик на строке задачи в таблице

Срабатывает обработчик handleDoubleClickTask()

Этап 2 - Открытие окна редактирования

Создаётся новое окно Stage

Заполняются поля редактирования с текущими значениями:

TextField для названия

TextArea для описания

Spinner для приоритета

TextField для даты (с автозаполнением)

ComboBox для статуса

Кнопки "Сохранить" и "Отмена"

Этап 3 - Редактирование

Пользователь изменяет нужные поля

Автозаполнение даты работает и в этом окне

Тема окна применяется автоматически (светлая или тёмная)

Этап 4 - Сохранение

Пользователь нажимает "Сохранить"

Обновляются поля объекта Task

Вызывается TaskService.updateTask()

Сервис устанавливает updatedAt = NOW()

Repository сохраняет через save()

Этап 5 - Обновление БД

Выполняется SQL UPDATE в таблицу tasks

WHERE условие по id задачи

Строка в БД обновляется

Этап 6 - Обновление UI

Окно редактирования закрывается

Строка в таблице обновляется автоматически

Пользователь видит изменения

Alert "Задача обновлена"

Модель данных (Entity классы)
Task (таблица: tasks)
Главная сущность приложения - представляет одну задачу в системе.

Поля:

id (BIGINT) - уникальный идентификатор (Primary Key)

title (VARCHAR 255) - название/заголовок задачи

description (TEXT) - подробное описание задачи

due_date (TIMESTAMP) - срок выполнения

created_at (TIMESTAMP) - когда задача была создана

updated_at (TIMESTAMP) - когда задача была последний раз изменена

status (VARCHAR 50) - текущий статус (NEW, IN_PROGRESS, COMPLETED, CANCELLED)

priority (INTEGER, 0-10) - приоритет (0 = низкий, 10 = высокий)

recurrence_type (VARCHAR 50) - тип повтора (для будущих версий)

recurrence_interval (INTEGER) - интервал повтора в днях

version (INTEGER) - для оптимистичной блокировки

Методы для UI:

isOverdue() - просрочена ли задача

isTodayOrTomorrow() - задача на сегодня или завтра

isThisWeek() - задача на текущую неделю

Alert (таблица: alerts)
Оповещения и напоминания, связанные с задачами.

Поля:

id (BIGINT) - уникальный идентификатор

task_id (BIGINT) - Foreign Key на задачу (связь Many-to-One)

alert_time (TIMESTAMP) - когда должно сработать оповещение

type (VARCHAR 50) - тип: NOTIFICATION, REMINDER, DEADLINE

message (TEXT) - текст сообщения оповещения

is_read (BOOLEAN) - прочитано ли оповещение

created_at (TIMESTAMP) - когда было создано оповещение

Связь:

Один Alert связан с одной Task

Одна Task может иметь много Alert

AudioFile (таблица: audio_files)
Хранилище аудиозаписей, связанных с задачами.

Поля:

id (BIGINT) - уникальный идентификатор

task_id (BIGINT) - Foreign Key на задачу (UNIQUE - один аудио на задачу)

audio_data (BYTEA) - бинарные данные аудиофайла

duration_seconds (INTEGER) - длительность записи в секундах

file_name (VARCHAR 255) - имя файла

created_at (TIMESTAMP) - когда был загружен файл

expires_at (TIMESTAMP) - когда файл будет удалён (по умолчанию +30 дней)

Связь:

One-to-One с Task (один аудиофайл на одну задачу максимум)

Сервисный слой
TaskService - управление задачами
Метод	Параметры	Возвращает	Описание
createTask()	title, description, priority, dueDate, recurrenceType	Task	Создаёт новую задачу
getTask()	id	Task	Получает задачу по ID
getAllTasks()	—	List<Task>	Возвращает все задачи
getTasksByStatus()	status	List<Task>	Фильтр по статусу
updateTask()	task	Task	Сохраняет изменения задачи
updateTaskStatus()	id, status	void	Меняет статус задачи
deleteTask()	id	void	Удаляет задачу по ID
getImportantTasks()	—	List<Task>	Задачи с приоритетом > 5
getActiveTasks()	—	List<Task>	Не завершённые задачи
Используемые компоненты:

@Autowired TaskRepository - для доступа к БД

@Service - аннотация Spring для компонента сервиса

AlertService - управление оповещениями
Метод	Параметры	Возвращает	Описание
createAlert()	task, alertTime, type, message	Alert	Создаёт оповещение
getUnreadAlerts()	—	List<Alert>	Непрочитанные оповещения
markAsRead()	alertId	void	Отмечает как прочитанное
deleteOldAlerts()	before	void	Удаляет старые оповещения
AudioFileService - работа с аудиофайлами
Метод	Параметры	Возвращает	Описание
saveAudioFile()	task, audioData, fileName, duration	AudioFile	Сохраняет аудиофайл
getAudioFile()	task	AudioFile	Получает аудио по задаче
deleteExpiredAudio()	—	void	Удаляет истёкшие аудио
UI-слой (JavaFX)
Компоненты main-view.fxml
Левая панель (форма создания):

Заголовок "СОЗДАНИЕ ЗАДАЧИ"

TextField для названия задачи

TextArea для описания (обязательное поле - красная метка)

Spinner для приоритета (значения 0-10)

TextField для даты выполнения (с автозаполнением)

ComboBox для типа повтора

VBox для интервала повтора (скрыт по умолчанию)

Кнопка "Создать задачу"

Правая панель (таблица и оповещения):

Кнопка "Удалить задачу"

ComboBox "Фильтр по статусу" (ALL, NEW, IN_PROGRESS, COMPLETED, CANCELLED)

Кнопка смены темы (🌙/☀)

TableView с колонками: Название, Статус, Приоритет, Срок

Заголовок "ОПОВЕЩЕНИЯ"

Label с количеством непрочитанных оповещений

ListView для отображения оповещений

MainController.java - ключевые методы
Инициализация:

initialize() - вызывается автоматически при загрузке FXML

Настраивает колонки таблицы

Устанавливает row factory для цветовой подсветки

Инициализирует автозаполнение дат

Загружает задачи из БД

Запускает фоновый поток обновления оповещений

Автозаполнение даты:

setupDateTimeInputMask() - устанавливает listeners для поля даты

autoFillDateTime() - парсит текст типа "0812" → "08.12.2026 00:00"

Работает на потере фокуса

Работает при нажатии Enter

Валидирует дату (31 февраля → текущая дата)

Цветовая подсветка задач:

setupTableRowFactory() - создаёт custom row factory

Красный фон - просроченные задачи

Жёлтый фон - задачи на сегодня/завтра

Синий фон - задачи на текущую неделю

Без цвета - остальные задачи

Смена темы:

handleToggleTheme() - переключает между светлой и тёмной темой

Применяется ко всем компонентам

Применяется к всплывающим окнам редактирования

Кнопка меняется 🌙 ↔ ☀

Редактирование задачи:

handleDoubleClickTask() - слушатель двойного клика

openTaskDetailWindow() - открывает окно редактирования

Создаёт новый Stage

Заполняет поля текущими значениями

Применяет текущую тему

Включает автозаполнение даты

На кнопке "Сохранить" вызывает update

Интеграция компонентов
Как работает Spring Boot + JavaFX
Spring Boot инициализирует:

Загружает application.properties с конфигом БД

Создаёт Spring контекст

Инициализирует сервисы (TaskService, AlertService, AudioFileService)

Создаёт репозитории через Spring Data JPA

Инициализирует JavaFX приложение

Передаёт Spring бины в FXMLLoader через controllerFactory

Создаёт MainController как Spring bean

MainController получает:

@Autowired TaskService - для работы с задачами

@Autowired AlertService - для работы с оповещениями

Вызывает методы сервисов для CRUD операций

База данных (PostgreSQL)
Таблица tasks
sql
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
Индексы для оптимизации:

idx_tasks_status - ускоряет фильтрацию по статусу

idx_tasks_due_date - ускоряет сортировку по дате

idx_tasks_priority - ускоряет поиск важных задач

Таблица alerts
sql
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
Индексы:

idx_alerts_is_read - быстрый поиск непрочитанных оповещений

idx_alerts_time - сортировка по времени

Таблица audio_files
sql
CREATE TABLE audio_files (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT UNIQUE NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    audio_data BYTEA NOT NULL,
    duration_seconds INTEGER,
    file_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP
);
UNIQUE constraint:

На task_id - гарантирует один аудиофайл на задачу

ON DELETE CASCADE - при удалении задачи удаляется и её аудиофайл

Ключевые технические решения
Spring Data JPA - автоматический CRUD без писания SQL

Hibernate - автоматическое маппирование объектов на таблицы

@Service/@Component - управление зависимостями через Spring

@Autowired - внедрение зависимостей

FXMLLoader - разделение UI разметки и логики

ObservableList - автоматическое обновление таблицы при изменении данных

Platform.runLater() - потокобезопасное обновление UI

CSS стили - динамическая смена темы

LocalDateTime - работа с датами и временем

Enum для статусов - типобезопасность вместо строк

Оптимизация и производительность
Индексы в PostgreSQL
Индексы ускоряют выполнение запросов:

sql
-- Ускоряют WHERE status = 'NEW'
CREATE INDEX idx_tasks_status ON tasks(status);

-- Ускоряют ORDER BY due_date
CREATE INDEX idx_tasks_due_date ON tasks(due_date);

-- Ускоряют WHERE priority > 5
CREATE INDEX idx_tasks_priority ON tasks(priority);
Кэширование (будущее улучшение)
java
@Cacheable("tasks")
public List<Task> getAllTasks() {
    return taskRepository.findAll();
}
Batch операции
java
// Вместо 100 запросов делаем 1
taskRepository.saveAll(tasks);
Безопасность
SQL Injection
✅ Защита: Spring Data JPA использует PreparedStatements автоматически

Оптимистичная блокировка
java
@Version
private Integer version;  // Hibernate проверяет версию при UPDATE
Предотвращает конфликты при одновременном редактировании.

Планы развития (будущие версии)
Версия 2.0.0
Повторяющиеся задачи (DAILY, WEEKLY, MONTHLY)

Категории и теги для группировки

Экспорт/импорт задач (JSON, CSV, XML)

Системные уведомления (Windows/macOS/Linux)

Горячие клавиши (Ctrl+N для новой задачи)

Версия 3.0.0
REST API для мобильного приложения

Web-интерфейс (React)

Синхронизация между устройствами

Облачное хранилище

Версия документа: 1.5.0
Последнее обновление: 08.01.2026
Статус: ✅ ГОТОВО