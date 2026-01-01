# 📖 README - Voice Task Manager (v2.0.0 с Telegram Bot)

## 🎯 Основные функции

### ✅ ФАЗА 1 (Активна)
- ✅ Ввод задач через JavaFX форму
- ✅ Создание, редактирование, удаление задач
- ✅ Система приоритетов (0–10)
- ✅ Статусы задач (NEW, IN_PROGRESS, COMPLETED, CANCELLED)
- ✅ Напоминания и уведомления
- ✅ Фильтрация и сортировка по статусу и приоритету
- ✅ История аудиозаписей (до 30 дней в БД)
- ✅ PostgreSQL персистентность

### ✅ ФАЗА 2 (НОВАЯ - Telegram Voice Input)
- ✅ **REST API для Voice Input** (7 endpoints)
- ✅ **Telegram Bot интеграция** (@voice_task_manager_bot)
- ✅ **Автоматический парсинг текста:**
  - Парсинг дат на русском (Natty): "завтра" → 02.01.2026
  - Парсинг приоритета: "приоритет 8" → priority: 8
  - Парсинг срочности: "срочно!" → priority: 8 автоматически
  - Очистка текста от служебных слов
- ✅ **Команды Telegram:**
  - `/start` - приветствие
  - `/today` - задачи на сегодня
  - `/list` - все задачи
  - `/stats` - статистика
  - `/search` - поиск
  - Просто напиши текст → создаст задачу!

### 🔄 В ПЛАНАХ (ФАЗА 3)
- Повторяющиеся задачи (ежедневно, еженедельно, ежемесячно)
- Категории и теги
- Улучшенный ML для контекста
- Web dashboard (React)
- Mobile app интеграция

---

## 🛠️ Технологический стек

### Frontend/UI
- **JavaFX 21** — Desktop приложение (ФАЗА 1)
- **Telegram Bot API** — Voice Input через Telegram (ФАЗА 2)
- **REST API** — Communication между компонентами

### Backend
- **Java 21** — основной язык
- **Spring Boot 3.2** — фреймворк, DI, конфигурация
- **Spring Data JPA** — ORM для работы с БД
- **Maven 3.8+** — управление зависимостями

### Voice Parsing (ФАЗА 2)
- **Natty 0.13** — парсинг дат на естественном языке
- **Java Regex** — парсинг приоритета и служебных слов
- **Custom String Processing** — очистка и нормализация текста

### Telegram Integration (ФАЗА 2)
- **PyTelegramBotAPI 4.x** (Python бот) ИЛИ
- **TelegramBots 7.0.1** (Java бот)

### База данных
- **PostgreSQL 12+** — реляционная БД
- **Hibernate 6** — ORM (Object-Relational Mapping)
- **HikariCP** — пул соединений

### DevTools
- **Git** — контроль версий
- **IntelliJ IDEA** / **VS Code** — IDE
- **Maven** — сборка

---

## 📋 Структура проекта

```text
task_manager/
├── pom.xml                             # Maven конфигурация + Natty, Telegram зависимости
├── README.md                           # Этот файл
├── LICENSE
├── .gitignore
│
├── docs/
│   ├── SETUP.md                        # Подробное руководство по установке
│   ├── ARCHITECTURE.md                 # Описание архитектуры
│   └── TELEGRAM_SETUP.md               # Telegram Bot инструкция (НОВАЯ)
│
├── src/main/
│   ├── java/com/taskmanager/
│   │   ├── TaskManagerApp.java         # Точка входа приложения
│   │   │
│   │   ├── config/
│   │   │   └── DatabaseConfig.java
│   │   │
│   │   ├── dao/
│   │   │   ├── TaskRepository.java
│   │   │   ├── AlertRepository.java
│   │   │   └── AudioFileRepository.java
│   │   │
│   │   ├── model/
│   │   │   ├── Task.java
│   │   │   ├── Alert.java
│   │   │   ├── AudioFile.java
│   │   │   └── *Status / *Type enums
│   │   │
│   │   ├── service/
│   │   │   ├── TaskService.java         # ОБНОВЛЕНА: методы для Voice Input
│   │   │   ├── AlertService.java
│   │   │   ├── AudioFileService.java
│   │   │   └── VoiceParsingService.java # НОВАЯ: парсинг текста
│   │   │
│   │   ├── controller/
│   │   │   ├── MainController.java      # JavaFX UI контроллер
│   │   │   └── VoiceTaskController.java # НОВАЯ: REST API (7 endpoints)
│   │   │
│   │   ├── dto/                         # НОВАЯ папка
│   │   │   ├── VoiceTaskRequest.java
│   │   │   ├── VoiceTaskParsed.java
│   │   │   ├── VoiceTaskResponse.java
│   │   │   └── TaskStatisticsDTO.java
│   │   │
│   │   └── bot/ (опционально для Java бота)
│   │       ├── TelegramBotService.java
│   │       └── TelegramBotConfig.java
│   │
│   └── resources/
│       ├── application.properties      # Конфиг (+ Telegram settings)
│       ├── fxml/
│       │   └── main-view.fxml
│       ├── css/
│       │   └── style.css
│       └── db/
│           └── schema.sql
│
├── target/                             # Скомпилированные артефакты
└── test/                               # Unit тесты
```

---

## 🚀 Быстрый старт (ФАЗА 1 + ФАЗА 2)

### Системные требования

- **Java 21+** — https://www.oracle.com/java/technologies/downloads/
- **PostgreSQL 12+** — https://www.postgresql.org/download/
- **Maven 3.8+** — https://maven.apache.org/download.cgi
- **Git** — https://git-scm.com/

### Шаг 1: Клонирование и Setup

```bash
git clone https://github.com/Adam-Rubinstein/task_manager.git
cd task_manager

# Создать БД
psql -U postgres
CREATE DATABASE taskmanager;
\q

# Инициализировать схему
psql -U postgres -d taskmanager -f src/main/resources/db/schema.sql
```

### Шаг 2: Конфигурация

Отредактируй `src/main/resources/application.properties`:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=postgres
spring.datasource.password=ВАШ_ПАРОЛЬ

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ФАЗА 2: Telegram Bot
telegram.bot.enabled=true
telegram.bot.token=YOUR_BOT_TOKEN_HERE
telegram.bot.username=voice_task_manager_bot
voice.parsing.language=ru
voice.parsing.date-format=dd.MM.yyyy HH:mm

# Логирование
logging.level.root=INFO
logging.level.com.taskmanager=DEBUG
```

### Шаг 3: Сборка и Запуск

```bash
# Сборка
mvn clean install

# Запуск (JavaFX ФАЗА 1)
mvn javafx:run

# В отдельном терминале: Telegram Bot (ФАЗА 2)
python telegram_bot.py
```

**При успешном старте:**
- Откроется JavaFX окно с таблицей задач
- Telegram бот будет готов к приемке сообщений
- REST API доступен на `http://localhost:8080/api/voice/*`

---

## 📚 REST API ENDPOINTS (ФАЗА 2)

```bash
# Создать задачу из текста
POST /api/voice/create-task
{
  "text": "Купить молоко завтра в 15:00, приоритет 8",
  "telegramUserId": 123456789
}
Response:
{
  "success": true,
  "message": "✅ Задача 'Купить молоко' создана на 02.01.2026 15:00",
  "task": { id, title, dueDate, priority, status, ... }
}

# Получить статистику
GET /api/voice/stats
Response:
{
  "totalTasks": 5,
  "newTasks": 2,
  "completedTasks": 2,
  "activeTasks": 3
}

# Задачи на сегодня
GET /api/voice/today
Response: [ { id, title, dueDate, ... }, ... ]

# Просроченные задачи
GET /api/voice/overdue
Response: [ { id, title, dueDate, ... }, ... ]

# Последние N задач
GET /api/voice/list?limit=5
Response: [ { id, title, ... }, ... ]

# Поиск по ключевому слову
GET /api/voice/search?q=молоко
Response: [ { id, title, ... }, ... ]

# Активные задачи (не завершены)
GET /api/voice/active
Response: [ { id, title, ... }, ... ]
```

---

## 🎤 Telegram Примеры

```
Ты в Telegram:
  "Купить молоко завтра в 15:00, приоритет 8"
  
Бот отвечает:
  "✅ Задача 'Купить молоко' создана!
   📅 Дата: 02.01.2026 15:00
   🔴 Приоритет: 8"

Ты:
  "/stats"
  
Бот:
  "📊 СТАТИСТИКА
   📈 Всего задач: 5
   🆕 Новых: 2
   ✅ Завершённых: 2
   ⏳ Активных: 3"
```

---

## 🔧 Архитектура (ФАЗА 2)

### Поток данных

```
Telegram User
    ↓ (пишет текст)
Telegram Bot (@voice_task_manager_bot)
    ↓ (HTTP POST /api/voice/create-task)
VoiceTaskController (REST)
    ↓
VoiceParsingService
    ├─ Natty парсит дату: "завтра в 15:00" → LocalDateTime
    ├─ Regex парсит приоритет: "приоритет 8" → 8
    └─ Возвращает VoiceTaskParsed (DTO)
    ↓
TaskService.createTaskFromVoice(VoiceTaskParsed)
    ↓ (save)
TaskRepository
    ↓ (insert)
PostgreSQL (таблица tasks)
    ↓
VoiceTaskResponse (success, message, task)
    ↓ (HTTP 200 + JSON)
Telegram Bot
    ↓ (отправляет в чат)
Telegram User видит: ✅ Задача создана!
```

### Слои архитектуры

```
┌─────────────────────────────────────┐
│ Telegram / REST API                 │ ← User Input
├─────────────────────────────────────┤
│ VoiceTaskController (REST)          │ ← API Layer
├─────────────────────────────────────┤
│ VoiceParsingService + TaskService   │ ← Business Logic
├─────────────────────────────────────┤
│ TaskRepository (Spring Data JPA)    │ ← Data Access
├─────────────────────────────────────┤
│ PostgreSQL (задачи)                 │ ← Persistence
└─────────────────────────────────────┘
```

---

## 🧪 Тестирование

### Запуск юнит-тестов

```bash
mvn test
```

### Тест REST API через curl

```bash
# Создание задачи
curl -X POST http://localhost:8080/api/voice/create-task \
  -H "Content-Type: application/json" \
  -d '{"text":"Test task tomorrow at 14:00","telegramUserId":123}'

# Получение статистики
curl http://localhost:8080/api/voice/stats

# Поиск
curl http://localhost:8080/api/voice/search?q=test
```

### Тест через Telegram

1. Найди бота в Telegram: @voice_task_manager_bot
2. Пришли сообщение: "Купить молоко завтра"
3. Получи ответ: ✅ Задача создана!

---

## 📄 Документация

- **[SETUP.md](docs/SETUP.md)** — подробная установка и решение проблем
- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** — описание архитектуры (обновлена для ФАЗЫ 2)
- **[TELEGRAM_SETUP.md](docs/TELEGRAM_SETUP.md)** — инструкция по Telegram боту

---

## 🎯 Версии

| Версия | Дата | Статус | Что нового |
|--------|------|--------|-----------|
| 1.0.0 | 01.12.2024 | ✅ Released | ФАЗА 1: JavaFX UI + CRUD |
| 2.0.0 | 01.01.2026 | ✅ Released | ФАЗА 2: Telegram + Voice Input |
| 3.0.0 | TBD | 🔄 Planning | ФАЗА 3: Smart Parsing + Categories |

---

## 📄 Лицензия

All Rights Reserved — см. файл [LICENSE](LICENSE)

Авторское право защищено. Копирование, модификация, распространение без письменного разрешения запрещены.

---

## 👨‍💻 Автор

**Adam Rubinstein**  
**GitHub:** [@Adam-Rubinstein](https://github.com/Adam-Rubinstein/)

---

**Версия:** 2.0.0  
**Последнее обновление:** 01.01.2026  
**Статус:** ✅ В активной разработке