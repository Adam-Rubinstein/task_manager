# 🚀 Установка и Запуск Voice Task Manager v2.0.0 (с Telegram Bot)

Подробное пошаговое руководство по развёртыванию проекта с поддержкой ФАЗЫ 2 (Voice Input via Telegram).

---

## 1. Предварительные требования

### Обязательное ПО
- **Java 21+** — https://www.oracle.com/java/technologies/downloads/#java21
- **Maven 3.8+** — https://maven.apache.org/download.cgi
- **PostgreSQL 12+** — https://www.postgresql.org/download/ (рекомендуется 14+)
- **Git** — https://git-scm.com/

### Для Telegram Bot (ФАЗА 2)
- **Python 3.8+** (если используешь Python бот) — https://www.python.org/downloads/
  ИЛИ используй Java бот (встроен в Spring Boot)
- **Telegram аккаунт** и **Telegram Bot** (см. шаг 6)

### Проверка установки

```bash
java -version
mvn -version
psql --version
python --version  # если используешь Python бот
git --version
```

---

## 2. Установка и запуск PostgreSQL

### Windows

1. Скачай инсталлятор: https://www.postgresql.org/download/windows/
2. Запусти инсталлятор, запомни пароль пользователя `postgres`
3. Убедись, что сервис PostgreSQL запущен (Services → PostgreSQL)

### macOS (Homebrew)

```bash
brew install postgresql@15
brew services start postgresql@15
psql postgres  # проверка подключения
```

### Linux (Ubuntu/Debian)

```bash
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib
sudo service postgresql start
sudo -u postgres psql  # проверка подключения
```

---

## 3. Создание базы данных

Подключись к PostgreSQL:

```bash
psql -U postgres
```

Создай БД:

```sql
CREATE DATABASE taskmanager;
\q
```

Проверка:

```bash
psql -U postgres -l  # должна быть видна taskmanager
```

---

## 4. Клонирование репозитория

```bash
git clone https://github.com/Adam-Rubinstein/task_manager.git
cd task_manager
```

---

## 5. Конфигурация приложения (ФАЗА 1 + ФАЗА 2)

Открой `src/main/resources/application.properties` и отредактируй:

### ФАЗА 1: PostgreSQL Connection

```properties
# PostgreSQL подключение
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=postgres
spring.datasource.password=ВАШ_ПАРОЛЬ_POSTGRES

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
```

### ФАЗА 2: Telegram Bot (НОВОЕ)

Добавь после PostgreSQL конфига:

```properties
# Telegram Bot Settings (ФАЗА 2)
telegram.bot.enabled=true
telegram.bot.token=YOUR_BOT_TOKEN_HERE
telegram.bot.username=voice_task_manager_bot

# Voice Parsing Settings
voice.parsing.language=ru
voice.parsing.date-format=dd.MM.yyyy HH:mm
```

**⚠️ ВАЖНО:** Не коммитьте реальные токены в Git!

Используй вместо этого переменные окружения:

```bash
# Для Linux/macOS
export TELEGRAM_BOT_TOKEN="123456789:ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefgh"

# Для Windows PowerShell
$env:TELEGRAM_BOT_TOKEN="123456789:ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefgh"

# В application.properties используй:
telegram.bot.token=${TELEGRAM_BOT_TOKEN}
```

---

## 6. Создание Telegram Bot (ФАЗА 2)

### Шаг 1: BotFather

1. Открой Telegram
2. Найди пользователя **@BotFather**
3. Отправь команду: `/newbot`
4. Выбери имя для бота (например: "Voice Task Manager Bot")
5. Выбери username (например: `voice_task_manager_bot`)
6. **BotFather выдаст токен** — скопируй его!

Пример токена:
```
123456789:ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefgh
```

### Шаг 2: Вставь токен в application.properties

```properties
telegram.bot.token=123456789:ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefgh
telegram.bot.username=voice_task_manager_bot
```

### Шаг 3: Протестируй

После запуска приложения найди своего бота в Telegram и отправь `/start`.
Если бот ответит — всё настроено правильно!

---

## 7. Инициализация схемы БД (опционально)

SQL-скрипт `src/main/resources/db/schema.sql` содержит полную схему с индексами и триггерами.

Запусти (опционально):

```bash
psql -U postgres -d taskmanager -f src/main/resources/db/schema.sql
```

**Примечание:** Если не выполнять этот скрипт, Hibernate с `ddl-auto=update` создаст таблицы автоматически, но без дополнительных индексов и триггеров.

---

## 8. Добавление зависимостей Maven

Убедись, что в `pom.xml` добавлены зависимости для ФАЗЫ 2:

```xml
<!-- Natty для парсинга дат -->
<dependency>
    <groupId>com.joestelmach</groupId>
    <artifactId>natty</artifactId>
    <version>0.13</version>
</dependency>

<!-- Telegram Bot API (если используешь Java бот) -->
<dependency>
    <groupId>org.telegram</groupId>
    <artifactId>telegrambots</artifactId>
    <version>7.0.1</version>
</dependency>
```

Выполни:

```bash
mvn clean install
```

---

## 9. Сборка проекта

```bash
mvn clean install
```

Или без тестов (быстрее):

```bash
mvn clean install -DskipTests
```

Проверка компиляции:

```bash
mvn clean compile
```

---

## 10. Запуск приложения

### Вариант 1: Maven JavaFX (для ФАЗЫ 1, рекомендуется для разработки)

```bash
mvn javafx:run
```

Откроется окно JavaFX с таблицей задач.

### Вариант 2: Spring Boot (для ФАЗЫ 2 REST API)

```bash
mvn spring-boot:run
```

Приложение будет работать в фоновом режиме, REST API доступен на `http://localhost:8080`.

### Вариант 3: Запуск обоих одновременно (рекомендуется для полной функциональности)

**Окно 1 (JavaFX UI ФАЗА 1):**

```bash
mvn javafx:run
```

**Окно 2 (REST API для ФАЗЫ 2, в новом терминале):**

```bash
mvn spring-boot:run
```

**Окно 3 (Telegram Bot, в новом терминале, если Python):**

```bash
python telegram_bot.py
```

(Если используешь Java бот, он запустится вместе с Spring Boot)

### Вариант 4: IntelliJ IDEA

1. Импортируй проект как Maven
2. Убедись, что выбран JDK 21 (Project Settings → Project → SDK)
3. Создай конфигурацию запуска:
   - **Type:** Application
   - **Main class:** `com.taskmanager.TaskManagerApp`
   - **Program arguments:** пусто
   - **VM options:** `--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media`
4. Нажми **Run** или **Shift+F10**

---

## 11. Успешный запуск

### ФАЗА 1 (JavaFX)
- Откроется окно приложения
- Таблица будет пустой (или с задачами, если они уже в БД)
- Вверху форма для создания задач
- Внизу счётчик оповещений

### ФАЗА 2 (Telegram Bot)
- В терминале увидишь логи: "Bot started polling"
- Найди бота в Telegram по username
- Отправь сообщение: "Купить молоко завтра в 15:00"
- Бот отвечает: "✅ Задача 'Купить молоко' создана!"

### REST API доступен
- Проверка: `curl http://localhost:8080/api/voice/stats`
- Должен вернуть JSON со статистикой

---

## 12. Типовые проблемы и решения

### ❌ «Cannot connect to database»

**Причины:**
- PostgreSQL не запущен
- Неверные учётные данные в `application.properties`
- БД не существует

**Решение:**

```bash
# Проверка PostgreSQL
# Windows
tasklist | findstr postgres

# macOS/Linux
ps aux | grep postgres

# Проверка существования БД
psql -U postgres -l

# Перезапуск PostgreSQL
# Windows: Services → PostgreSQL → Restart
# macOS: brew services restart postgresql@15
# Linux: sudo service postgresql restart
```

---

### ❌ «relation "tasks" does not exist»

**Причина:** Таблицы не созданы

**Решение:**
1. Запусти приложение один раз с `ddl-auto=update` — Hibernate создаст таблицы
2. Или вручную запусти: `psql -U postgres -d taskmanager -f src/main/resources/db/schema.sql`

---

### ❌ «JavaFX runtime components are missing»

**Причины:**
- Используешь JRE вместо JDK
- Неправильная версия Java (не 21)
- Отсутствуют JavaFX модули

**Решение:**

```bash
# Проверка версии Java
java -version

# Убедись, что JDK 21, а не JRE
# Переустанови JDK если нужно

# Для Maven используй:
mvn javafx:run  # плагин автоматически настраивает модули

# Для IDE укажи VM options:
--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media
```

---

### ❌ «No visible @SpringBootConfiguration class»

**Причина:** Spring не находит `@SpringBootApplication`

**Решение:**

```bash
mvn clean compile
# или перезагрузи IDE (Ctrl+Shift+S)
```

---

### ❌ Telegram Bot не отвечает

**Проверки:**

1. Правильный ли токен в `application.properties`?
   ```bash
   echo $TELEGRAM_BOT_TOKEN  # (macOS/Linux)
   echo %TELEGRAM_BOT_TOKEN% # (Windows)
   ```

2. Запущено ли приложение?
   ```bash
   curl http://localhost:8080/api/voice/stats
   ```

3. Telegram Bot имеет ли доступ в интернет?

4. Логи показывают ошибки?
   ```bash
   # Увеличь логирование в application.properties
   logging.level.com.taskmanager=DEBUG
   ```

---

### ❌ Natty не парсит русские даты

**Причина:** Natty по умолчанию на английском

**Решение:** Используется кастомная реализация в `VoiceParsingService`:
- Сначала заменяем русские даты на английские: "завтра" → "tomorrow"
- Потом передаём Natty
- Результат используем

Если не работает — проверь логи:

```
logging.level.com.taskmanager=DEBUG
```

---

## 13. Разработка

### Запуск тестов

```bash
mvn test
```

### Создание feature-ветки

```bash
git checkout -b feature/my-feature
# ... изменения ...
git add .
git commit -m "feat: описание фичи"
git push origin feature/my-feature
```

### Горячее перезагрузка (DevTools)

Добавь в `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

---

## 14. Дополнительные ресурсы

- **JavaFX:** https://openjfx.io/
- **Spring Boot:** https://spring.io/projects/spring-boot
- **Hibernate:** https://hibernate.org/
- **PostgreSQL:** https://www.postgresql.org/docs/
- **Maven:** https://maven.apache.org/guides/
- **Telegram Bot API:** https://core.telegram.org/bots/api
- **Natty:** https://natty.joestelmach.com/

---

## 15. Полезные команды

```bash
# Сборка и запуск всё сразу
mvn clean javafx:run

# Только сборка
mvn clean install

# Сборка без тестов (быстро)
mvn clean install -DskipTests

# Проверка синтаксиса
mvn clean compile

# Запуск конкретного теста
mvn test -Dtest=TaskServiceTest

# Чистка проекта
mvn clean

# Проверка зависимостей
mvn dependency:tree

# PostgreSQL команды
psql -U postgres -d taskmanager  # подключение
\dt                               # список таблиц
SELECT * FROM task;              # вывод задач
\q                                # выход
```

---

**Версия документа:** 2.0.0  
**Обновлено:** 01.01.2026  
**Статус:** ✅ ГОТОВО К ИСПОЛЬЗОВАНИЮ