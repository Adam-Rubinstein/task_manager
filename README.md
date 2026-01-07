📋 Task Manager - Менеджер Задач v1.5.0
Настольное приложение для управления задачами с JavaFX интерфейсом и PostgreSQL базой данных.

🎯 Основные возможности
✅ Управление задачами
Создание задач с названием и описанием

Редактирование задач (двойной клик на задаче)

Удаление задач с подтверждением

Система приоритетов (0-10, настраивается Spinner)

Статусы задач: NEW, IN_PROGRESS, COMPLETED, CANCELLED

Фильтрация по статусу (ComboBox)

Сортировка по любой колонке таблицы

⏰ Даты и время
Автозаполнение даты - введи 0812 → автоматически заполнится 08.12.2026 00:00

Работает на потере фокуса (переход на другое поле)

Работает по нажатию Enter

Валидация дат (31 февраля → текущая дата)

Формат ввода: dd.MM.yyyy HH:mm

🎨 Интерфейс
Светлая/тёмная тема (кнопка 🌙/☀ в правом верхнем углу)

Цветовая подсветка задач:

🔴 Красный фон - просроченные задачи

🟡 Жёлтый фон - задачи на сегодня/завтра

🔵 Синий фон - задачи на эту неделю

Responsive дизайн - автоматическое масштабирование

🔔 Система оповещений
Счётчик непрочитанных оповещений

Список уведомлений (двойной клик → отметить как прочитанное)

Автообновление каждые 10 секунд

Типы оповещений: NOTIFICATION, REMINDER, DEADLINE

🔁 Повторяющиеся задачи (заготовка)
Типы повтора: NONE, DAILY, WEEKLY, MONTHLY, CUSTOM

Интервал повтора (для CUSTOM) - скрывается, если выбран "Без повтора"

🛠️ Технологический стек
Frontend/UI
JavaFX 21 - Desktop приложение

FXML - разметка интерфейса

CSS - стилизация (темная/светлая тема)

Backend
Java 21 - основной язык

Spring Boot 3.2 - фреймворк, Dependency Injection

Spring Data JPA - ORM для работы с БД

Hibernate 6 - JPA провайдер

Maven 3.8+ - управление зависимостями

База данных
PostgreSQL 12+ - реляционная БД

HikariCP - пул соединений

DevTools
Git - контроль версий

IntelliJ IDEA - рекомендуемая IDE

📋 Структура проекта
text
TaskManager/
├── pom.xml                             # Maven конфигурация
├── README.md                           # Этот файл
├── SETUP.md                            # Подробная инструкция по установке
├── ARCHITECTURE.md                     # Описание архитектуры
│
├── src/main/
│   ├── java/com/taskmanager/
│   │   ├── TaskManagerApp.java         # Точка входа
│   │   │
│   │   ├── config/
│   │   │   ├── DatabaseConfig.java     # Конфигурация БД
│   │   │   └── ThemeManager.java       # Управление темами
│   │   │
│   │   ├── dao/
│   │   │   ├── TaskRepository.java
│   │   │   ├── AlertRepository.java
│   │   │   └── AudioFileRepository.java
│   │   │
│   │   ├── model/
│   │   │   ├── Task.java               # Сущность задачи
│   │   │   ├── Alert.java              # Оповещения
│   │   │   ├── AudioFile.java          # Аудиофайлы (резерв)
│   │   │   ├── TaskStatus.java         # Enum статусов
│   │   │   ├── AlertType.java          # Enum типов оповещений
│   │   │   └── RecurrenceType.java     # Enum типов повторов
│   │   │
│   │   ├── service/
│   │   │   ├── TaskService.java        # Бизнес-логика задач
│   │   │   ├── AlertService.java       # Бизнес-логика оповещений
│   │   │   └── AudioFileService.java   # Работа с аудио
│   │   │
│   │   └── ui/controllers/
│   │       └── MainController.java     # JavaFX контроллер
│   │
│   └── resources/
│       ├── application.properties      # Конфигурация приложения
│       ├── fxml/
│       │   └── main-view.fxml          # Разметка интерфейса
│       ├── css/
│       │   └── style.css               # Стили
│       └── db/
│           └── schema.sql              # SQL схема БД
│
└── target/                             # Скомпилированные файлы
🚀 Быстрый старт
Предварительные требования
Java 21+ - https://www.oracle.com/java/technologies/downloads/

PostgreSQL 12+ - https://www.postgresql.org/download/

Maven 3.8+ - https://maven.apache.org/download.cgi

Git - https://git-scm.com/

Проверка установки
bash
java -version      # Должна быть 21+
mvn -version       # Должна быть 3.8+
psql --version     # Должна быть 12+
Шаг 1: Клонирование репозитория
bash
git clone https://github.com/Adam-Rubinstein/task_manager.git
cd task_manager
Шаг 2: Создание базы данных
bash
# Подключиться к PostgreSQL
psql -U postgres

# Создать БД
CREATE DATABASE taskmanager;
\q

# Проверка
psql -U postgres -l  # должна быть видна taskmanager
Опционально: Запустить SQL схему вручную:

bash
psql -U postgres -d taskmanager -f src/main/resources/db/schema.sql
⚠️ Примечание: Если не запускать schema.sql, Hibernate создаст таблицы автоматически при первом запуске (с ddl-auto=update), но без дополнительных индексов и триггеров.

Шаг 3: Конфигурация
Отредактируй src/main/resources/application.properties:

text
# PostgreSQL подключение
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=postgres
spring.datasource.password=ВАШ_ПАРОЛЬ_ЗДЕСЬ

spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Логирование
logging.level.root=INFO
logging.level.com.taskmanager=DEBUG
logging.level.org.hibernate=WARN
Шаг 4: Сборка проекта
bash
mvn clean install
Или без тестов (быстрее):

bash
mvn clean install -DskipTests
Шаг 5: Запуск приложения
Вариант 1: Maven JavaFX (рекомендуется)
bash
mvn javafx:run
Вариант 2: IntelliJ IDEA
Импортируй проект как Maven

Убедись, что выбран JDK 21 (Project Settings → Project → SDK)

Создай конфигурацию запуска:

Type: Application

Main class: com.taskmanager.TaskManagerApp

VM options: --add-modules javafx.controls,javafx.fxml,javafx.graphics

Нажми Run (Shift+F10)

Вариант 3: JAR файл
bash
mvn clean package
java --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar target/TaskManager-1.5.0.jar
При успешном запуске:
✅ Откроется окно приложения
✅ Таблица будет пустой (или с задачами, если они уже в БД)
✅ Вверху форма для создания задач
✅ Справа вверху кнопка смены темы 🌙
✅ Внизу счётчик оповещений

📖 Как пользоваться
Создание задачи
Название (опционально) - если не заполнить, возьмётся первая строка описания

Описание (обязательно, красная метка) - основной текст задачи

Приоритет (0-10) - чем выше, тем важнее (по умолчанию 5)

Дата выполнения - формат dd.MM.yyyy HH:mm

Введи 0812 → автоматически станет 08.12.2026 00:00

Работает при потере фокуса или по Enter

Тип повтора - NONE (без повтора), DAILY, WEEKLY, MONTHLY, CUSTOM

Нажми "Создать задачу"

Редактирование задачи
Двойной клик на строке задачи в таблице

Откроется окно редактирования

Измени поля (название, описание, приоритет, статус, дата)

Нажми "Сохранить" или "Отмена"

Удаление задачи
Выдели задачу в таблице

Нажми "Удалить задачу"

Подтверди удаление в диалоге

Фильтрация задач
Используй ComboBox "Фильтр по статусу" справа вверху

Выбери: ALL, NEW, IN_PROGRESS, COMPLETED, CANCELLED

Смена темы
Нажми кнопку 🌙 в правом верхнем углу

Интерфейс переключится на тёмную тему (кнопка станет ☀)

Нажми ещё раз для возврата к светлой теме

Оповещения
Счётчик непрочитанных оповещений отображается внизу

Двойной клик на оповещении → отметить как прочитанное

Обновление автоматическое каждые 10 секунд

🔧 Разработка
Запуск тестов
bash
mvn test
Создание feature-ветки
bash
git checkout -b feature/my-feature
# ... изменения ...
git add .
git commit -m "feat: описание фичи"
git push origin feature/my-feature
Горячая перезагрузка (DevTools)
Добавь в pom.xml:

xml
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-devtools</artifactId>
<scope>runtime</scope>
<optional>true</optional>
</dependency>
🐛 Решение проблем
❌ «Cannot connect to database»
Причины:

PostgreSQL не запущен

Неверные учётные данные в application.properties

БД не существует

Решение:

bash
# Проверка PostgreSQL (Windows)
tasklist | findstr postgres

# Проверка PostgreSQL (macOS/Linux)
ps aux | grep postgres

# Проверка существования БД
psql -U postgres -l

# Перезапуск PostgreSQL
# Windows: Services → PostgreSQL → Restart
# macOS: brew services restart postgresql
# Linux: sudo service postgresql restart
❌ «relation "tasks" does not exist»
Причина: Таблицы не созданы

Решение:

Запусти приложение один раз с ddl-auto=update - Hibernate создаст таблицы

Или вручную: psql -U postgres -d taskmanager -f src/main/resources/db/schema.sql

❌ «JavaFX runtime components are missing»
Причина: Используешь JRE вместо JDK или неправильная версия Java

Решение:

bash
# Проверка версии Java
java -version  # Должна быть 21+

# Убедись, что JDK 21, а не JRE
# Переустанови JDK если нужно

# Для Maven используй:
mvn javafx:run  # плагин автоматически настраивает модули

# Для IDE укажи VM options:
--add-modules javafx.controls,javafx.fxml,javafx.graphics
❌ Автозаполнение даты не работает
Проверка:

Введи несколько цифр (например 0812)

Нажми Enter или кликни на другое поле

Должно автоматически заполниться 08.12.2026 00:00

Если не работает:

Проверь, что используешь новую версию MainController.java

Проверь логи в консоли (возможна ошибка парсинга)

❌ Тёмная тема не применяется к всплывающему окну
Решение:

Обновлена в последней версии

Окно редактирования задачи теперь учитывает текущую тему

📚 Документация
SETUP.md - подробное руководство по установке

ARCHITECTURE.md - описание архитектуры проекта

🎯 Версии
Версия	Дата	Статус	Что нового
1.0.0	01.12.2024	✅ Released	Базовый CRUD + JavaFX UI
1.2.0	15.12.2024	✅ Released	Система оповещений + фильтрация
1.5.0	08.01.2026	✅ Released	Автозаполнение даты + тёмная тема + редактирование
📊 Статистика проекта
Языки: Java 21, FXML, SQL

Строк кода: ~3000+

Зависимостей: 12 (Maven)

Таблиц БД: 3 (tasks, alerts, audio_files)

📄 Лицензия
All Rights Reserved

Авторское право защищено. Копирование, модификация, распространение без письменного разрешения запрещены.

👨‍💻 Автор
Adam Rubinstein
GitHub: @Adam-Rubinstein
Email: adam.rubinstein@example.com

🔮 Планы развития
Версия 2.0.0 (в планах)
Повторяющиеся задачи (ежедневные, еженедельные, ежемесячные)

Категории и теги задач

Экспорт/импорт задач (JSON, CSV)

Системные уведомления (Windows/macOS/Linux)

Горячие клавиши (Ctrl+N для новой задачи и т.д.)

Версия 3.0.0 (далёкое будущее)
Web-интерфейс (React + REST API)

Mobile приложение (синхронизация)

Голосовой ввод через Telegram Bot

ML для автоматической категоризации

Версия документа: 1.5.0
Последнее обновление: 08.01.2026
Статус: ✅ Готов к использованию