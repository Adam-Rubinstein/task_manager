# 🏗️ Архитектура Voice Task Manager

## Обзор

**Voice Task Manager** — настольное приложение для управления задачами, разработанное на Java 21, Spring Boot и JavaFX.

Текущая версия реализует:
- создание, редактирование и удаление задач;
- хранение задач, оповещений и аудиофайлов в PostgreSQL;
- отображение и фильтрацию задач в JavaFX UI;
- систему оповещений с пометкой как прочитанных.

Часть схемы БД и зависимостей зарезервирована под будущие фичи: голосовой ввод, повторения, теги, синхронизация.

---

## Слоистая архитектура

```text
┌───────────────────────────────────────────────┐
│ Presentation Layer (JavaFX UI)               │
│ - main-view.fxml                             │
│ - MainController (@Component)                │
└───────────────────┬──────────────────────────┘
                    │
┌───────────────────▼──────────────────────────┐
│ Service Layer (Business Logic)               │
│ - TaskService (@Service)                     │
│ - AlertService (@Service)                    │
│ - AudioFileService (@Service)                │
└───────────────────┬──────────────────────────┘
                    │
┌───────────────────▼──────────────────────────┐
│ Data Access Layer (DAO / Repository)         │
│ - TaskRepository (Spring Data JPA)           │
│ - AlertRepository (Spring Data JPA)          │
│ - AudioFileRepository (Spring Data JPA)      │
└───────────────────┬──────────────────────────┘
                    │
┌───────────────────▼──────────────────────────┐
│ Persistence Layer (Database)                 │
│ - PostgreSQL + Hibernate / JPA               │
│ - schema.sql (расширенная схема)            │
└──────────────────────────────────────────────┘
```

---

## Модульная структура

```text
com.taskmanager/
├── TaskManagerApp.java                 # Точка входа (@SpringBootApplication + JavaFX)
│
├── config/
│   └── DatabaseConfig.java             # Конфигурация БД (полагается на application.properties)
│
├── dao/                                # Доступ к данным (Spring Data JPA)
│   ├── TaskRepository.java
│   ├── AlertRepository.java
│   └── AudioFileRepository.java
│
├── model/                              # JPA-сущности и enum-ы
│   ├── Task.java
│   ├── Alert.java
│   ├── AudioFile.java
│   ├── TaskStatus.java                 # Статусы задач (NEW, IN_PROGRESS, COMPLETED, CANCELLED)
│   ├── AlertType.java                  # Типы оповещений
│   └── RecurrenceType.java             # Типы повторений (подготовлено)
│
├── service/                            # Бизнес-логика
│   ├── TaskService.java                # CRUD для задач и фильтрация
│   ├── AlertService.java               # Управление оповещениями
│   └── AudioFileService.java           # Работа с аудиофайлами
│
└── ui/controllers/
    └── MainController.java             # JavaFX-контроллер, работает как Spring bean
```

---

## Модель данных (Entity-классы)

### `Task`

Минимальная модель задачи, привязанная к таблице `tasks`:

```text
Task
├── id           : Long                  # Primary Key
├── title        : String                # Название (обязательно)
├── description  : String                # Описание (TEXT)
├── dueDate      : LocalDateTime         # Срок выполнения (обязательно)
├── createdAt    : LocalDateTime         # Дата создания (обязательно)
├── status       : TaskStatus            # Статус (NEW, IN_PROGRESS, COMPLETED, CANCELLED)
├── priority     : Integer               # Приоритет 0–10
└── updatedAt    : LocalDateTime         # Дата последнего обновления
```

В `schema.sql` таблица `tasks` содержит дополнительные поля
(`recurrence_*`, `version`, `tags` через отдельную таблицу и др.) — зарезервированы под будущее развитие.

### `Alert`

Оповещение по задаче, таблица `alerts`:

```text
Alert
├── id          : Long                   # Primary Key
├── taskId      : Long                   # ID связанной задачи (FK)
├── alertTime   : LocalDateTime          # Когда сработать
├── type        : AlertType              # Тип оповещения
├── message     : String                 # Текст сообщения
├── isRead      : Boolean                # Прочитано ли
└── createdAt   : LocalDateTime          # Дата создания
```

### `AudioFile`

Хранение аудиоданных, таблица `audio_files`:

```text
AudioFile
├── id              : Long               # Primary Key
├── audioData       : byte[]             # Бинарные данные (BYTEA)
├── durationSeconds : Integer            # Длительность в секундах
├── createdAt       : LocalDateTime      # Дата загрузки
├── fileName        : String             # Имя файла
└── taskId          : Long               # ID связанной задачи (FK, UNIQUE)
```

---

## Сервисный слой

### `TaskService`

Отвечает за:
- создание задач (валидация, установка `createdAt`, дефолтного статуса и приоритета);
- получение всех задач;
- фильтрацию по статусу;
- удаление задач;
- (в перспективе) обновление, работа с повторениями и тегами.

### `AlertService`

Отвечает за:
- создание оповещений по задачам;
- получение списка непрочитанных оповещений;
- пометку оповещений как прочитанных;
- (в будущем) генерацию уведомлений на основе сроков.

### `AudioFileService`

Отвечает за:
- сохранение аудиофайлов в БД (BYTEA);
- выборку аудиозаписей по задаче;
- (в будущем) очистку устаревших записей, интеграцию со Speech-to-Text.

---

## UI-слой (JavaFX)

### `main-view.fxml`

Описывает основное окно приложения:
- таблица задач (колонки: название, статус, приоритет, срок);
- поля ввода для создания новой задачи (название, описание, приоритет);
- комбобокс для фильтрации по статусу;
- счётчик и список непрочитанных оповещений;
- кнопки: Создать задачу, Удалить задачу.

### `MainController`

Контроллер UI, помечен как `@Component`, зависимости внедряются через Spring:
- инициализирует таблицу, комбобоксы и спиннеры при старте;
- загружает все задачи из БД при инициализации;
- по кнопке «Создать» вызывает `TaskService.createTask()` и обновляет таблицу;
- по кнопке «Удалить» удаляет выбранную задачу;
- по изменению фильтра статуса перезапрашивает только нужные задачи;
- каждые 10 секунд в отдельном потоке обновляет счётчик и список оповещений.

---

## Потоки данных (основные сценарии)

### 1. Создание задачи

```text
Пользователь вводит название/описание/приоритет в форме
      ↓
MainController.handleCreateTask()
      ↓
TaskService.createTask(title, description, dueDate, priority)
      ↓
TaskRepository.save(task)
      ↓
PostgreSQL (таблица tasks)
      ↓
Задача добавляется в ObservableList и таблица обновляется
```

### 2. Фильтрация по статусу

```text
Пользователь выбирает статус в ComboBox
      ↓
MainController.handleFilterByStatus()
      ↓
TaskService.getTasksByStatus(status)
      ↓
TaskRepository.findByStatus(...)
      ↓
Результат подставляется в ObservableList / TableView
```

### 3. Оповещения (фоновое обновление)

```text
Фоновый поток в MainController (обновление каждые 10 сек)
      ↓
AlertService.getUnreadAlerts()
      ↓
AlertRepository.findByIsReadFalse()
      ↓
Platform.runLater() обновляет UI:
  - Label "Оповещения: N"
  - ListView со списком сообщений
```

---

## База данных (PostgreSQL)

Основная схема хранится в `src/main/resources/db/schema.sql`.

Ключевые таблицы:

```text
tasks
├── id (BIGSERIAL PK)
├── title, description, due_date
├── status (VARCHAR 50)
├── priority (INTEGER 0–10)
├── created_at, updated_at
├── recurrence_* (зарезервировано)
├── version (для синхронизации)

alerts
├── id (BIGSERIAL PK)
├── task_id (FK → tasks.id)
├── alert_time
├── alert_type
├── message
├── is_read
├── created_at

audio_files
├── id (BIGSERIAL PK)
├── task_id (FK → tasks.id, UNIQUE)
├── audio_data (BYTEA)
├── file_size
├── created_at
├── expires_at (автоочистка через 30 дней)
```

Дополнительно:
- индексы на `due_date`, `status`, `priority`, `created_at`, `is_read`;
- триггер для автоматического обновления `updated_at` в `tasks`;
- функция для очистки просроченных записей в `audio_files`.

---

## Интеграция Spring Boot + JavaFX

- `TaskManagerApp` помечен `@SpringBootApplication`, поднимает Spring-контекст.
- JavaFX-часть использует `FXMLLoader` с `controllerFactory`, которая берёт контроллеры как Spring-beans.
- `MainController` и сервисы живут в одном DI-контейнере, упрощает тестирование и расширение.
- `@ComponentScan(basePackages = {"com.taskmanager"})` обязателен для поиска всех компонентов.

---

## Планы развития

Документация и схема БД уже заложили следующие направления:

- **Голосовой ввод** — интеграция со Speech-to-Text API;
- **Парсинг естественного языка** — извлечение дат, приоритетов из текста (Natty);
- **Повторяющиеся задачи** — ежедневные, еженедельные, ежемесячные задачи;
- **Теги и поиск** — полнотекстовый поиск, группировка по тегам;
- **Синхронизация** — сохранение в облако, доступ с разных устройств;
- **Desktop-уведомления** — системные уведомления и звуковые напоминания.

Текущая архитектура (слои + Spring Data JPA) позволяет добавлять эти фичи без ломки существующего кода.

---

## Ключевые технические решения

1. **Spring Data JPA** — репозитории наследуются от `JpaRepository`, автоматически получают CRUD методы.
2. **Hibernate с `ddl-auto=update`** — схема создаётся/обновляется автоматически при запуске.
3. **FXMLLoader + Spring beans** — контроллеры JavaFX внедряются как Spring компоненты, полная интеграция DI.
4. **Асинхронное обновление UI** — `Platform.runLater()` для безопасных обновлений UI из фоновых потоков.
5. **ObservableList + TableView** — реактивное обновление таблицы при изменении данных.

---

**Версия документа:** 1.0.0  
**Обновлено:** 1 января 2026
