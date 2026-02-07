# 🛠️ SETUP.md - Подробная инструкция по установке

Пошаговое руководство по установке и настройке Voice Task Manager

---

## 📋 Содержание

- [Системные требования](#системные-требования)
- [Установка зависимостей](#установка-зависимостей)
- [Настройка базы данных](#настройка-базы-данных)
- [Конфигурация приложения](#конфигурация-приложения)
- [Сборка и запуск](#сборка-и-запуск)
- [Решение проблем](#решение-проблем)

---

## 💻 Системные требования

### Минимальные требования

- **ОС:** Windows 10/11, macOS 10.14+, Linux (Ubuntu 20.04+)
- **Процессор:** 2 ядра, 2 GHz
- **ОЗУ:** 4 GB
- **Свободное место:** 500 MB

### Рекомендуемые требования

- **ОС:** Windows 11, macOS 12+, Linux (Ubuntu 22.04+)
- **Процессор:** 4 ядра, 3 GHz
- **ОЗУ:** 8 GB
- **Свободное место:** 1 GB

---

## 📥 Установка зависимостей

### 1. Установка Java 21

#### Windows

- Скачай [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/)
- Запусти установщик
- Добавь в PATH:
  - `Панель управления → Система → Дополнительные параметры системы → Переменные среды`
  - Добавь `JAVA_HOME` → `C:\Program Files\Java\jdk-21`
  - Добавь в `Path` → `%JAVA_HOME%\bin`
- Проверка:
  ```bash
  java -version
  ```

#### macOS

```bash
# Через Homebrew
brew install openjdk@21

# Добавить в PATH (в ~/.zshrc или ~/.bash_profile)
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"

# Проверка
java -version
```

#### Linux (Ubuntu/Debian)

```bash
# Обновить пакеты
sudo apt update

# Установить JDK 21
sudo apt install openjdk-21-jdk

# Проверка
java -version
```

---

### 2. Установка PostgreSQL 12+

#### Windows

- Скачай [PostgreSQL](https://www.postgresql.org/download/windows/)
- Запусти установщик
- Запомни пароль для пользователя `postgres`
- Проверка:
  ```bash
  psql --version
  ```

#### macOS

```bash
# Через Homebrew
brew install postgresql@15

# Запустить службу
brew services start postgresql@15

# Проверка
psql --version
```

#### Linux (Ubuntu/Debian)

```bash
# Установка
sudo apt install postgresql postgresql-contrib

# Запуск службы
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Проверка
psql --version
```

---

### 3. Установка Maven 3.8+

#### Windows

- Скачай [Apache Maven](https://maven.apache.org/download.cgi)
- Распакуй в `C:\Program Files\Apache\maven`
- Добавь в PATH:
  - `MAVEN_HOME` → `C:\Program Files\Apache\maven`
  - В `Path` → `%MAVEN_HOME%\bin`
- Проверка:
  ```bash
  mvn -version
  ```

#### macOS

```bash
# Через Homebrew
brew install maven

# Проверка
mvn -version
```

#### Linux (Ubuntu/Debian)

```bash
# Установка
sudo apt install maven

# Проверка
mvn -version
```

---

### 4. Установка Git

#### Windows

- Скачай [Git for Windows](https://git-scm.com/download/win)
- Запусти установщик
- Проверка:
  ```bash
  git --version
  ```

#### macOS

```bash
# Через Homebrew
brew install git

# Проверка
git --version
```

#### Linux (Ubuntu/Debian)

```bash
# Установка
sudo apt install git

# Проверка
git --version
```

---

## 🗄️ Настройка базы данных

### Создание базы данных

#### Способ 1: Через psql (командная строка)

```bash
# Подключиться к PostgreSQL
psql -U postgres

# Создать базу данных
CREATE DATABASE taskmanager;

# Создать пользователя (опционально)
CREATE USER taskmanager_user WITH PASSWORD 'your_password';

# Дать права
GRANT ALL PRIVILEGES ON DATABASE taskmanager TO taskmanager_user;

# Выйти
\q
```

#### Способ 2: Через pgAdmin (GUI)

- Открой pgAdmin
- Правый клик на `Databases` → `Create` → `Database...`
- Введи имя: `taskmanager`
- Нажми `Save`

---

### Запуск SQL схемы

```bash
# Перейти в директорию проекта
cd task_manager

# Запустить схему
psql -U postgres -d taskmanager -f src/main/resources/db/schema.sql
```

**Примечание:** Если не запускать `schema.sql`, Hibernate создаст таблицы автоматически.

---

## ⚙️ Конфигурация приложения

### Редактирование application.properties

Открой файл `src/main/resources/application.properties`:

```properties
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
```

🔐 **Важно (безопасность):**
Не храни в репозитории `spring.datasource.password` и `telegram.bot.token` — в текущем `application.properties` они присутствуют в явном виде, лучше вынести в переменные окружения/секреты и использовать шаблон `application-example.properties`.


### Параметры конфигурации

| Параметр | Описание | Значение по умолчанию |
|----------|----------|----------------------|
| `spring.datasource.url` | URL базы данных | `jdbc:postgresql://localhost:5432/taskmanager` |
| `spring.datasource.username` | Имя пользователя БД | `postgres` |
| `spring.datasource.password` | Пароль пользователя БД | (не указан) |
| `spring.jpa.hibernate.ddl-auto` | Режим обновления схемы | `update` |
| `spring.jpa.show-sql` | Показывать SQL запросы | `false` |

---

## 🚀 Сборка и запуск

### Клонирование репозитория

```bash
git clone https://github.com/Adam-Rubinstein/task_manager.git
cd task_manager
```

---

### Сборка проекта

```bash
# Полная сборка
mvn clean install

# Или без тестов (быстрее)
mvn clean install -DskipTests
```

---

### Запуск приложения

#### Вариант 1: Maven (рекомендуется)

```bash
mvn javafx:run
```

#### Вариант 2: JAR файл

```bash
# Собрать JAR
mvn clean package

# Запустить
java --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar target/TaskManager-1.5.0.jar
```

#### Вариант 3: IntelliJ IDEA

- Открой проект в IntelliJ IDEA
- `File → Project Structure → Project → SDK` → выбери JDK 21
- Создай конфигурацию запуска:
  - `Run → Edit Configurations → + → Application`
  - **Main class:** `com.taskmanager.TaskManagerApp`
  - **VM options:** `--add-modules javafx.controls,javafx.fxml,javafx.graphics`
  - **Module:** `TaskManager`
- Нажми `Run` (Shift+F10)

---

## 🐛 Решение проблем

### Проблема: «Cannot connect to database»

**Возможные причины:**

- PostgreSQL не запущен
- Неверные учётные данные
- База данных не существует
- Порт 5432 занят

**Решение:**

```bash
# Проверка статуса PostgreSQL (Windows)
tasklist | findstr postgres

# Проверка статуса PostgreSQL (macOS/Linux)
ps aux | grep postgres

# Перезапуск PostgreSQL (Windows)
# Services → PostgreSQL → Restart

# Перезапуск PostgreSQL (macOS)
brew services restart postgresql

# Перезапуск PostgreSQL (Linux)
sudo systemctl restart postgresql

# Проверка существования БД
psql -U postgres -l
```

---

### Проблема: «JavaFX runtime components are missing»

**Возможные причины:**

- Установлен JRE вместо JDK
- Неправильная версия Java
- Не указаны VM options

**Решение:**

```bash
# Проверка версии Java
java -version

# Должно быть: openjdk version "21.x.x"
# Если нет — переустанови JDK 21

# Для Maven используй:
mvn javafx:run

# Для IDE добавь VM options:
--add-modules javafx.controls,javafx.fxml,javafx.graphics
```

---

### Проблема: «BUILD FAILURE» при сборке Maven

**Возможные причины:**

- Нет интернет-соединения
- Maven не может загрузить зависимости
- Конфликт версий

**Решение:**

```bash
# Очистить кэш Maven
mvn clean

# Принудительно обновить зависимости
mvn clean install -U

# Если не помогает — удалить кэш вручную
# Windows: удали C:\Users\<Пользователь>\.m2\repository
# macOS/Linux: удали ~/.m2/repository
```

---

### Проблема: «Port 5432 is already in use»

**Возможные причины:**

- Другое приложение использует порт 5432
- Несколько экземпляров PostgreSQL

**Решение:**

```bash
# Найти процесс, использующий порт (Windows)
netstat -ano | findstr :5432

# Найти процесс, использующий порт (macOS/Linux)
lsof -i :5432

# Убить процесс (macOS/Linux)
kill -9 <PID>

# Или изменить порт в application.properties:
spring.datasource.url=jdbc:postgresql://localhost:5433/taskmanager
```

---

### Проблема: «Schema "public" does not exist»

**Возможные причины:**

- База данных создана некорректно
- Нет прав доступа

**Решение:**

```bash
# Подключиться к БД
psql -U postgres -d taskmanager

# Создать схему
CREATE SCHEMA IF NOT EXISTS public;

# Дать права
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

# Выйти
\q
```

---

## ✅ Проверка установки

После успешного запуска должно открыться окно приложения:

- ✅ Окно Voice Task Manager открылось
- ✅ Нет ошибок в консоли
- ✅ Можно создать задачу
- ✅ Задача сохраняется в БД
- ✅ Смена темы работает

---

## 📚 Дополнительные ресурсы

- [Официальная документация Java](https://docs.oracle.com/en/java/)
- [Документация JavaFX](https://openjfx.io/)
- [Документация Spring Boot](https://spring.io/projects/spring-boot)
- [Документация PostgreSQL](https://www.postgresql.org/docs/)
- [Документация Maven](https://maven.apache.org/guides/)

---

**Версия документа:** 1.5.0  
**Последнее обновление:** 08.01.2026  
**Статус:** ✅ Готов к использованию
