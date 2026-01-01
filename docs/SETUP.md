# 🚀 Руководство по установке и запуску Voice Task Manager

## Предварительные требования

- **Java 21+** - [Скачать](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.6+** - [Скачать](https://maven.apache.org/download.cgi)
- **PostgreSQL 12+** - [Скачать](https://www.postgresql.org/download/)
- **Git** - [Скачать](https://git-scm.com/)

## Пошаговая установка

### 1. Проверка установки Java и Maven

```bash
java -version
mvn -version
```

Должны вывести информацию о версиях.

### 2. Установка и запуск PostgreSQL

#### На Windows:
1. Скачайте инсталлятор PostgreSQL с официального сайта
2. Запустите инсталлер и следуйте инструкциям
3. Запомните пароль для пользователя `postgres`
4. PostgreSQL запустится автоматически

#### На macOS (с Homebrew):
```bash
brew install postgresql@15
brew services start postgresql@15
```

#### На Linux (Ubuntu/Debian):
```bash
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib
sudo service postgresql start
```

### 3. Создание базы данных

```bash
# Подключитесь к PostgreSQL
psql -U postgres

# Выполните в psql:
CREATE DATABASE taskmanager;
\q
```

Или создайте через pgAdmin GUI.

### 4. Клонирование репозитория

```bash
git clone https://github.com/yourusername/voice-task-manager.git
cd voice-task-manager
```

### 5. Конфигурация приложения

Отредактируйте файл `src/main/resources/application.properties`:

```properties
# Основные настройки БД
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD  # <-- Измените на ваш пароль
```

Измените `YOUR_PASSWORD` на пароль, который вы установили при установке PostgreSQL.

### 6. Инициализация БД (опционально)

Если вы хотите инициализировать БД вручную:

```bash
psql -U postgres -d taskmanager -f src/main/resources/db/schema.sql
```

**Или** позвольте Hibernate создать таблицы автоматически (используется по умолчанию с `ddl-auto=update`).

### 7. Сборка проекта

```bash
mvn clean install
```

Эта команда:
- Скачивает все зависимости
- Компилирует исходный код
- Запускает тесты
- Создает JAR файл

### 8. Запуск приложения

#### Вариант 1: Через Maven (рекомендуется для разработки)
```bash
mvn javafx:run
```

#### Вариант 2: Через JAR файл
```bash
java -jar target/voice-task-manager-1.0.0-jar-with-dependencies.jar
```

#### Вариант 3: Через Maven exec плагин
```bash
mvn exec:java -Dexec.mainClass="com.taskmanager.Application"
```

## Успешный запуск

Если приложение запустилось успешно, вы увидите:
- Окно приложения "Voice Task Manager"
- Таблица с колонками: ID, Название, Срок, Статус, Приоритет
- Панель управления сверху для ввода задач и записи голоса

## Возможные проблемы и решения

### Ошибка: "Cannot connect to database"

**Решение:**
1. Проверьте, что PostgreSQL запущена:
   ```bash
   # На Windows (в Command Prompt)
   tasklist | findstr postgres
   
   # На macOS/Linux
   ps aux | grep postgres
   ```

2. Проверьте учетные данные в `application.properties`

3. Проверьте, что БД создана:
   ```bash
   psql -U postgres -l
   ```

### Ошибка: "Module not found"

**Решение:**
```bash
mvn clean install -DskipTests
```

### Ошибка при запуске JavaFX

**Решение:**
Убедитесь, что у вас установлена Java 21:
```bash
java -version
```

Если версия старше, установите Java 21.

### Порт 5432 уже используется

**Решение:**
Если PostgreSQL уже запущена на другом экземпляре:
```bash
# На Windows - найдите процесс
netstat -ano | findstr :5432

# На macOS/Linux
lsof -i :5432

# Либо измените порт в application.properties
spring.datasource.url=jdbc:postgresql://localhost:5433/taskmanager
```

## Разработка

### Запуск тестов

```bash
mvn test
```

### Создание новой feature ветки

```bash
git checkout -b feature/my-feature
# Делайте изменения
git add .
git commit -m "feat: добавил новую функцию"
git push origin feature/my-feature
```

### Сборка для распространения

```bash
mvn clean package assembly:single
```

Это создаст одноф JAR файл со всеми зависимостями в `target/`.

## Дополнительные ресурсы

- [JavaFX документация](https://openjfx.io/)
- [Hibernate документация](https://hibernate.org/)
- [PostgreSQL документация](https://www.postgresql.org/docs/)
- [Maven документация](https://maven.apache.org/guides/)

## Получение помощи

Если у вас есть вопросы:
1. Проверьте логи приложения в `logs/application.log`
2. Создайте Issue на GitHub
3. Посмотрите существующие Issues и Discussion

---

**Версия:** 1.0.0  
**Обновлено:** 31 декабря 2024
