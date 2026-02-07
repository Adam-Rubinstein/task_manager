# 🏗️ ARCHITECTURE.md - Архитектура проекта

Подробное описание архитектуры Voice Task Manager

---

## 📋 Содержание

- [Общая архитектура](#общая-архитектура)
- [Структура проекта](#структура-проекта)
- [Слои приложения](#слои-приложения)
- [Модель данных](#модель-данных)
- [Паттерны проектирования](#паттерны-проектирования)
- [Технологический стек](#технологический-стек)

---

## 🎯 Общая архитектура

Task Manager построен на основе **многослойной архитектуры** (Layered Architecture) с использованием паттерна **MVC** (Model-View-Controller).

### Диаграмма архитектуры

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                   │
│                    (JavaFX UI + FXML)                   │
│                   MainController.java                   │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│                     Business Layer                      │
│              (Service Classes + Logic)                  │
│      TaskService, AlertService, AudioFileService        │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│                   Persistence Layer                     │
│            (Spring Data JPA Repositories)               │
│   TaskRepository, AlertRepository, AudioFileRepository  │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│                     Data Layer                          │
│                 (PostgreSQL Database)                   │
│            tasks, alerts, audio_files                   │
└─────────────────────────────────────────────────────────┘
```

---

## 📂 Структура проекта

```
TaskManager/
│
├── 📄 pom.xml                                 # Maven конфигурация
├── 📄 README.md                               # Основная документация
│
├── 📁 src/
│   │
│   ├── 📁 main/
│   │   │
│   │   ├── 📁 java/com/taskmanager/
│   │   │   │
│   │   │   ├── 📄 TaskManagerApp.java         # 🚀 Точка входа
│   │   │   │
│   │   │   ├── 📁 config/                     # ⚙️ Конфигурация
│   │   │   │   ├── DatabaseConfig.java
│   │   │   │   └── ThemeManager.java
│   │   │   │
│   │   │   ├── 📁 dao/                        # 💾 Data Access Objects
│   │   │   │   ├── TaskRepository.java
│   │   │   │   ├── AlertRepository.java
│   │   │   │   └── AudioFileRepository.java
│   │   │   │
│   │   │   ├── 📁 model/                      # 📦 Модели данных
│   │   │   │   ├── Task.java
│   │   │   │   ├── Alert.java
│   │   │   │   ├── AudioFile.java
│   │   │   │   ├── TaskStatus.java
│   │   │   │   ├── AlertType.java
│   │   │   │   └── RecurrenceType.java
│   │   │   │
│   │   │   ├── 📁 service/                    # ⚡ Бизнес-логика
│   │   │   │   ├── TaskService.java
│   │   │   │   ├── AlertService.java
│   │   │   │   └── AudioFileService.java
│   │   │   │
│   │   │   └── 📁 ui/controllers/             # 🎮 UI контроллеры
│   │   │       └── MainController.java
│   │   │
│   │   └── 📁 resources/
│   │       ├── 📄 application.properties      # Конфигурация
│   │       ├── 📁 fxml/                       # JavaFX разметка
│   │       ├── 📁 css/                        # Стили
│   │       └── 📁 db/                         # SQL схемы
│   │
│   ├── 📁 test/                               # 🧪 Тесты
│   │
│   └── 📁 docks/                              # 📄 Документация
│       ├── 📄 SETUP.md                        # Инструкция по установке
│       └── 📄 ARCHITECTURE.md                 # Этот файл
│
└── 📁 target/                                 # ⚙️ Собранные файлы
```

---

## 🏛️ Слои приложения

### 1. Presentation Layer (Слой представления)

**Назначение:** Отвечает за пользовательский интерфейс и взаимодействие с пользователем

**Компоненты:**

- **JavaFX UI** - графический интерфейс
- **FXML файлы** - разметка интерфейса
- **CSS стили** - оформление
- **MainController** - контроллер UI

**Технологии:**

- JavaFX 21
- FXML
- CSS

**Принципы:**

- Не содержит бизнес-логики
- Только отображение и ввод данных
- Делегирует обработку на Service Layer

---

### 2. Business Layer (Слой бизнес-логики)

**Назначение:** Содержит бизнес-правила и логику приложения

**Компоненты:**

- **TaskService** - управление задачами
- **AlertService** - управление оповещениями
- **AudioFileService** - управление аудиофайлами

**Ответственность:**

- Валидация данных
- Бизнес-правила
- Координация между слоями
- Обработка транзакций

**Пример:**

```java
@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    public Task createTask(Task task) {
        // Валидация
        validateTask(task);
        
        // Бизнес-логика
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            task.setTitle(extractFirstLine(task.getDescription()));
        }
        
        // Сохранение
        return taskRepository.save(task);
    }
}
```

---

### 3. Persistence Layer (Слой доступа к данным)

**Назначение:** Управление взаимодействием с базой данных

**Компоненты:**

- **TaskRepository** - CRUD операции для задач
- **AlertRepository** - CRUD операции для оповещений
- **AudioFileRepository** - CRUD операции для аудио

**Технологии:**

- Spring Data JPA
- Hibernate
- HikariCP (пул соединений)

**Пример:**

```java
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByStatus(TaskStatus status);
    
    List<Task> findByDueDateBefore(LocalDateTime date);
    
    @Query("SELECT t FROM Task t WHERE t.status != 'COMPLETED' AND t.dueDate < :now")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);
}
```

---

### 4. Data Layer (Слой данных)

**Назначение:** Хранение данных

**Компоненты:**

- **PostgreSQL база данных**
- **Таблицы:** tasks, task_tags, alerts, audio_files

---

## 🗄️ Модель данных

### Диаграмма базы данных

```
┌─────────────────────────┐
│         tasks           │
├─────────────────────────┤
│ id (PK)                 │
│ title                   │
│ description             │
│ status                  │
│ priority                │
│ due_date                │
│ recurrence_type         │
│ recurrence_interval     │
│ created_at              │
│ updated_at              │
└─────────────────────────┘
            │
            │ 1:N
            ▼
┌─────────────────────────┐
│        alerts           │
├─────────────────────────┤
│ id (PK)                 │
│ task_id (FK)            │
│ type                    │
│ message                 │
│ is_read                 │
│ created_at              │
└─────────────────────────┘

┌─────────────────────────┐
│      audio_files        │
├─────────────────────────┤
│ id (PK)                 │
│ task_id (UNIQUE, FK)    │
│ audio_data (BYTEA)      │
│ file_size               │
│ created_at              │
│ expires_at              │
└─────────────────────────┘

```

---

### Описание таблиц

#### Таблица `tasks`

| Поле | Тип | Описание |
|------|-----|----------|
| `id` | BIGSERIAL | Первичный ключ |
| `title` | VARCHAR(255) | Название задачи |
| `description` | TEXT | Описание задачи |
| `status` | VARCHAR(50) | Статус (NEW, IN_PROGRESS, COMPLETED, CANCELLED) |
| `priority` | INTEGER | Приоритет (0-10) |
| `due_date` | TIMESTAMP | Дата выполнения |
| `recurrence_type` | VARCHAR(50) | Тип повтора |
| `recurrence_interval` | INTEGER | Интервал повтора |
| `created_at` | TIMESTAMP | Дата создания |
| `updated_at` | TIMESTAMP | Дата обновления |

#### Таблица `alerts`

| Поле | Тип | Описание |
|------|-----|----------|
| `id` | BIGSERIAL | Первичный ключ |
| `task_id` | BIGINT | Внешний ключ на tasks |
| `type` | VARCHAR(50) | Тип оповещения |
| `message` | TEXT | Текст оповещения |
| `is_read` | BOOLEAN | Прочитано или нет |
| `created_at` | TIMESTAMP | Дата создания |

#### Таблица `audio_files`

| Поле | Тип | Описание |
|------|-----|----------|
| `id` | BIGSERIAL | Первичный ключ |
| `file_name` | VARCHAR(255) | Имя файла |
| `file_path` | TEXT | Путь к файлу |
| `file_size` | BIGINT | Размер файла |
| `created_at` | TIMESTAMP | Дата создания |

---

## ⚠️ Несостыковки (важно)

- В `schema.sql` таблица задач называется `tasks`, а в JPA-модели `Task` указано `@Table(name = "task")` — при ручном прогоне схемы и JPA это нужно синхронизировать.
- В `schema.sql` таблица аудио называется `audio_files`, а в JPA-модели `AudioFile` используется другое имя таблицы — тоже требуется синхронизация.


## 🏗️ Паттерны проектирования

### 1. MVC (Model-View-Controller)

**Использование:**

- **Model:** `Task`, `Alert`, `AudioFile`
- **View:** FXML файлы + CSS
- **Controller:** `MainController`

**Преимущества:**

- Разделение ответственности
- Удобное тестирование
- Переиспользование компонентов

---

### 2. Repository Pattern

**Использование:**

- `TaskRepository`
- `AlertRepository`
- `AudioFileRepository`

**Преимущества:**

- Абстракция доступа к данным
- Централизованные запросы
- Легкая замена БД

---

### 3. Dependency Injection (DI)

**Использование:**

- Spring Boot `@Autowired`
- Инъекция зависимостей через конструктор

**Пример:**

```java
@Service
public class TaskService {
    
    private final TaskRepository taskRepository;
    
    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}
```

**Преимущества:**

- Слабая связанность
- Удобное тестирование
- Управление жизненным циклом

---

### 4. Singleton

**Использование:**

- `ThemeManager` (управление темами)
- Spring Bean по умолчанию

**Пример:**

```java
public class ThemeManager {
    
    private static ThemeManager instance;
    private boolean isDarkMode = false;
    
    private ThemeManager() {}
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
}
```

---

### 5. Observer Pattern

**Использование:**

- JavaFX Property Binding
- Автообновление UI при изменении данных

**Пример:**

```java
// Модель
private final SimpleIntegerProperty unreadCount = new SimpleIntegerProperty(0);

public IntegerProperty unreadCountProperty() {
    return unreadCount;
}

// UI
label.textProperty().bind(
    Bindings.concat("Непрочитанных: ", model.unreadCountProperty())
);
```

---

## 🛠️ Технологический стек

### Backend

| Технология | Версия | Назначение |
|------------|--------|------------|
| Java | 21 | Основной язык |
| Spring Boot | 3.2 | Фреймворк |
| Spring Data JPA | 3.2 | ORM |
| Hibernate | 6 | JPA провайдер |
| PostgreSQL | 12+ | База данных |
| HikariCP | - | Пул соединений |

### Frontend

| Технология | Версия | Назначение |
|------------|--------|------------|
| JavaFX | 21 | UI фреймворк |
| FXML | - | Разметка |
| CSS | - | Стилизация |

### Build & Tools

| Технология | Версия | Назначение |
|------------|--------|------------|
| Maven | 3.8+ | Сборка проекта |
| Git | - | Контроль версий |

---

## 🔄 Жизненный цикл запроса

### Создание задачи

```
1. User → UI (MainController)
   Пользователь нажимает "Создать задачу"

2. UI → TaskService
   controller.createTask() → taskService.createTask()

3. TaskService → Validation
   Валидация данных, применение бизнес-правил

4. TaskService → TaskRepository
   taskRepository.save(task)

5. TaskRepository → Database
   JPA/Hibernate выполняет INSERT

6. Database → TaskRepository
   Возвращает сохранённую задачу с ID

7. TaskRepository → TaskService
   Возвращает Task

8. TaskService → UI
   Обновление TableView

9. UI → User
   Отображение новой задачи
```

---

## 🧪 Тестирование

### Уровни тестирования

- **Unit Tests** - тестирование отдельных компонентов
- **Integration Tests** - тестирование взаимодействия слоёв
- **UI Tests** - тестирование пользовательского интерфейса

### Пример Unit теста

```java
@SpringBootTest
public class TaskServiceTest {
    
    @Autowired
    private TaskService taskService;
    
    @MockBean
    private TaskRepository taskRepository;
    
    @Test
    public void testCreateTask() {
        // Arrange
        Task task = new Task();
        task.setDescription("Test task");
        
        when(taskRepository.save(any(Task.class)))
            .thenReturn(task);
        
        // Act
        Task result = taskService.createTask(task);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test task", result.getDescription());
    }
}
```

---

## 🔒 Безопасность

### Текущая реализация

- Локальное приложение без аутентификации
- Защита на уровне БД (пароль PostgreSQL)

### Планы на будущее

- Аутентификация пользователей
- Шифрование чувствительных данных
- Защита от SQL-инъекций (через JPA)

---

## 📈 Масштабируемость

### Текущая архитектура

- Однопользовательское приложение
- Локальная БД

### Планы развития

- Миграция на клиент-серверную архитектуру
- REST API для web/mobile клиентов
- Multi-tenancy (поддержка нескольких пользователей)

---

## 📚 Дополнительные ресурсы

- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [JavaFX Documentation](https://openjfx.io/)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Версия документа:** 1.5.0  
**Последнее обновление:** 08.01.2026  
**Статус:** ✅ Готов к использованию
