# 🚀 Руководство по установке и запуску Voice Task Manager

Это подробное пошаговое руководство по развёртыванию проекта локально.

---

## 1. Предварительные требования

- **Java 21+**  
  Скачать: https://www.oracle.com/java/technologies/downloads/#java21
- **Maven 3.8+**  
  Скачать: https://maven.apache.org/download.cgi
- **PostgreSQL 12+** (рекомендуется 14+)  
  Скачать: https://www.postgresql.org/download/
- **Git**  
  Скачать: https://git-scm.com/

Проверь установку:

```bash
java -version
mvn -version
```

---

## 2. Установка и запуск PostgreSQL

### Windows

1. Скачай инсталлятор с официального сайта.
2. Установи PostgreSQL, запомни пароль пользователя `postgres`.
3. Убедись, что сервис PostgreSQL запущен (в службах Windows).

### macOS (Homebrew)

```bash
brew install postgresql@15
brew services start postgresql@15
```

### Linux (Ubuntu/Debian)

```bash
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib
sudo service postgresql start
```

---

## 3. Создание базы данных

Подключись к PostgreSQL:

```bash
psql -U postgres
```

Внутри `psql` выполни:

```sql
CREATE DATABASE taskmanager;
\q
```

---

## 4. Клонирование репозитория

```bash
git clone https://github.com/Adam-Rubinstein/task_manager.git
cd task_manager
```

---

## 5. Конфигурация приложения

Основной конфиг: `src/main/resources/application.properties`.

Пример:

```properties
# Подключение к PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=postgres
spring.datasource.password=ВАШ_ПАРОЛЬ
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

Рекомендации:

- Не коммить реальные пароли в Git.
- Для продакшена использовать переменные окружения.

---

## 6. Инициализация БД (schema.sql)

В проекте есть расширенный SQL-скрипт: `src/main/resources/db/schema.sql`.  
Он создаёт таблицы `tasks`, `alerts`, `audio_files`, индексы и триггеры.

Запуск:

```bash
psql -U postgres -d taskmanager -f src/main/resources/db/schema.sql
```

> Если не запускать этот скрипт, Hibernate с `ddl-auto=update` создаст минимально необходимые таблицы, но без дополнительных индексов и триггеров.

---

## 7. Сборка проекта

```bash
mvn clean install
```

Команда:

- скачает зависимости;
- скомпилирует код;
- выполнит тесты;
- соберёт JAR в `target/`.

Если тесты не важны:

```bash
mvn clean install -DskipTests
```

---

## 8. Запуск приложения

### Вариант 1 — Maven JavaFX (для разработки, рекомендуется)

```bash
mvn javafx:run
```

Maven сам подтянет необходимые JavaFX-модули.

### Вариант 2 — через Spring Boot

```bash
mvn spring-boot:run
```

### Вариант 3 — через JAR

```bash
# сборка
mvn clean package

# запуск
java -jar target/voice-task-manager-1.0.0.jar
```

### Вариант 4 — через IntelliJ IDEA

1. Импортируй проект как Maven.
2. Убедись, что выбран JDK 21.
3. Создай конфигурацию запуска:
   - **Type:** Application
   - **Main class:** `com.taskmanager.TaskManagerApp`
   - **VM options** (если нужно, для IDE без Maven плагина):

     ```text
     --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media
     ```

4. Запусти конфигурацию.

---

## 9. Успешный запуск

При успешном запуске:

- Откроется окно JavaFX приложения.
- В таблице появится список задач (из БД, может быть пустой).
- Вверху — форма для создания новых задач.
- Внизу — счётчик и список оповещений.

---

## 10. Типовые проблемы и решения

### «Cannot connect to database»

Проверь:

1. Что PostgreSQL запущен:

   ```bash
   # Windows
   tasklist | findstr postgres

   # macOS / Linux
   ps aux | grep postgres
   ```

2. Настройки в `application.properties` (URL, логин, пароль).
3. Что БД `taskmanager` существует:

   ```bash
   psql -U postgres -l
   ```

### «relation "tasks" does not exist»

- Либо не выполнил `schema.sql`,
- либо Hibernate ещё не создал таблицу.
- Решение: запусти приложение один раз с `ddl-auto=update` или прогон `schema.sql`.

### Проблемы с JavaFX («JavaFX runtime components are missing»)

- Убедись, что используешь JDK 21 (а не JRE).
- Для запуска из IDE укажи VM options с модулями JavaFX.
- Для Maven используй `mvn javafx:run` — плагины сами настраивают модули.

### Ошибка: «No visible @SpringBootConfiguration class»

- Проверь, что `TaskManagerApp.java` помечен `@SpringBootApplication`.
- Выполни `mvn clean compile`.
- Перезагрузи IDE.

---

## 11. Разработка

### Запуск тестов

```bash
mvn test
```

### Создание новой feature-ветки

```bash
git checkout -b feature/my-feature
# ... изменения ...
git add .
git commit -m "feat: описание фичи"
git push origin feature/my-feature
```

### Сборка «толстого» JAR для распространения

```bash
mvn clean package
```

(Spring Boot JAR будет уже self-contained, можно запустить просто `java -jar`.)

---

## 12. Дополнительные ресурсы

- JavaFX: https://openjfx.io/
- Spring Boot: https://spring.io/projects/spring-boot
- Hibernate: https://hibernate.org/
- PostgreSQL: https://www.postgresql.org/docs/
- Maven: https://maven.apache.org/guides/

---

**Версия документа:** 1.0.0  
**Обновлено:** 1 января 2026
