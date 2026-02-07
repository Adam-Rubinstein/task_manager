# 🛠️ SETUP.txt - Подробная инструкция по установке

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

Установи JDK 21+ и проверь:

```bash
java -version
```

---

### 2. Установка PostgreSQL 12+

Установи PostgreSQL и проверь:

```bash
psql --version
```

---

### 3. Установка Maven 3.8+

Установи Maven и проверь:

```bash
mvn -version
```

---

### 4. Установка Git

Установи Git и проверь:

```bash
git --version
```

---

## 🗄️ Настройка базы данных

### Создание базы данных

```bash
psql -U postgres
CREATE DATABASE taskmanager;
\q
```

---

### Запуск SQL схемы (рекомендуется)

`schema.sql` создаёт таблицы `tasks`, `task_tags`, `alerts`, `audio_files`, индексы и триггер автообновления `updated_at`.

```bash
cd task_manager
psql -U postgres -d taskmanager -f src/main/resources/db/schema.sql
```

**Примечание:** Если не запускать `schema.sql`, Hibernate создаст таблицы автоматически при первом запуске (с `ddl-auto=update`), но без дополнительных индексов и триггеров.

---

## ⚙️ Конфигурация приложения

Открой файл `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=postgres
spring.datasource.password=ВАШ_ПАРОЛЬ_ЗДЕСЬ

spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

🔐 **Важно (безопасность):**
Не коммить `spring.datasource.password` и `telegram.bot.token` — вынеси их в переменные окружения/секреты.

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
mvn clean install
```

Или без тестов:

```bash
mvn clean install -DskipTests
```

---

### Запуск приложения

#### Вариант 1: Maven

```bash
mvn javafx:run
```

#### Вариант 2: IntelliJ IDEA

- Main class: `com.taskmanager.TaskManagerApp`
- VM options: `--add-modules javafx.controls,javafx.fxml,javafx.graphics`

#### Вариант 3: JAR

```bash
mvn clean package
java --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar target/TaskManager-1.5.0.jar
```

---

## 🐛 Решение проблем

### Проблема: «Cannot connect to database»

Возможные причины:
- PostgreSQL не запущен
- Неверные учётные данные
- База данных не существует

Проверки:

```bash
psql -U postgres -l
```

---

### Проблема: «relation "tasks" does not exist»

Таблицы не созданы.
- Запусти приложение один раз с `ddl-auto=update`
- Или вручную прогони `schema.sql`

---

**Версия документа:** 1.5.0
**Последнее обновление:** 07.02.2026