🚀 Руководство по установке Task Manager v1.5.0
Пошаговая инструкция по развёртыванию JavaFX приложения с PostgreSQL базой данных.

1. Предварительные требования
   Обязательное ПО
   Java 21+ — https://www.oracle.com/java/technologies/downloads/#java21

Maven 3.8+ — https://maven.apache.org/download.cgi

PostgreSQL 12+ — https://www.postgresql.org/download/ (рекомендуется 14+)

Git — https://git-scm.com/

Проверка установки
bash
java -version     # Должна быть 21+
mvn -version      # Должна быть 3.8+
psql --version    # Должна быть 12+
git --version     # Любая версия
Пример вывода:

bash
$ java -version
openjdk version "21.0.1" 2023-10-17
OpenJDK Runtime Environment (build 21.0.1+12-29)
OpenJDK 64-Bit Server VM (build 21.0.1+12-29, mixed mode, sharing)

$ mvn -version
Apache Maven 3.9.6

$ psql --version
psql (PostgreSQL) 15.3
2. Установка и запуск PostgreSQL
   Windows
   Скачай инсталлятор: https://www.postgresql.org/download/windows/

Запусти инсталлятор

Важно: Запомни пароль пользователя postgres

Убедись, что сервис PostgreSQL запущен:

Открой Services (Win+R → services.msc)

Найди PostgreSQL

Статус должен быть Running

macOS (Homebrew)
bash
# Установка
brew install postgresql@15

# Запуск сервиса
brew services start postgresql@15

# Проверка подключения
psql postgres
Linux (Ubuntu/Debian)
bash
# Установка
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib

# Запуск сервиса
sudo service postgresql start

# Проверка подключения
sudo -u postgres psql
3. Создание базы данных
   Шаг 1: Подключись к PostgreSQL
   bash
   psql -U postgres
   При запросе пароля введи тот, что указал при установке.

Шаг 2: Создай базу данных
sql
CREATE DATABASE taskmanager;
Шаг 3: Проверь создание
sql
\l
Должна появиться строка:

text
taskmanager | postgres | UTF8     | ...
Шаг 4: Выйди из psql
sql
\q
Проверка из командной строки
bash
psql -U postgres -l | grep taskmanager
Должна быть видна БД taskmanager.

4. Клонирование репозитория
   bash
   git clone https://github.com/Adam-Rubinstein/task_manager.git
   cd task_manager
5. Конфигурация приложения
   Открой файл src/main/resources/application.properties и отредактируй:

Обязательные настройки PostgreSQL
text
# PostgreSQL подключение
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=postgres
spring.datasource.password=ВАШ_ПАРОЛЬ_POSTGRES_ЗДЕСЬ

spring.datasource.driver-class-name=org.postgresql.Driver
⚠️ ВАЖНО: Замени ВАШ_ПАРОЛЬ_POSTGRES_ЗДЕСЬ на реальный пароль!

Настройки JPA/Hibernate
text
# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
Пояснение:

ddl-auto=update — автоматическое создание/обновление таблиц

show-sql=false — не выводить SQL запросы (для production)

Для отладки можно поставить show-sql=true

Настройки логирования
text
# Логирование
logging.level.root=INFO
logging.level.com.taskmanager=DEBUG
logging.level.org.hibernate=WARN
Для отладки можно увеличить уровень:

text
logging.level.com.taskmanager=TRACE
logging.level.org.hibernate.SQL=DEBUG
6. Инициализация схемы БД (опционально)
   SQL-скрипт src/main/resources/db/schema.sql содержит полную схему с индексами и триггерами.

Запуск вручную (рекомендуется для production)
bash
psql -U postgres -d taskmanager -f src/main/resources/db/schema.sql
Автоматическое создание (для разработки)
Если не запускать schema.sql, Hibernate с ddl-auto=update создаст таблицы автоматически при первом запуске, но без дополнительных индексов и триггеров.

Что будет создано автоматически:

Таблицы: tasks, alerts, audio_files

Колонки и типы данных

Foreign Keys

Что НЕ будет создано автоматически:

Индексы для оптимизации (кроме первичных ключей)

Триггеры для updated_at

Check constraints

Рекомендация: Для production используй schema.sql вручную.

7. Сборка проекта
   Полная сборка с тестами
   bash
   mvn clean install
   Сборка без тестов (быстрее)
   bash
   mvn clean install -DskipTests
   Только компиляция (без упаковки JAR)
   bash
   mvn clean compile
   Ожидаемый результат:

text
[INFO] BUILD SUCCESS
[INFO] Total time:  15.234 s
[INFO] Finished at: 2026-01-08T02:30:00+03:00
8. Запуск приложения
   Вариант 1: Maven JavaFX Plugin (рекомендуется для разработки)
   bash
   mvn javafx:run
   Плюсы:

Автоматическая настройка JavaFX модулей

Не нужно указывать VM options вручную

Минусы:

Немного медленнее, чем прямой запуск

Вариант 2: IntelliJ IDEA
Шаг 1: Импорт проекта
File → Open

Выбери папку task_manager

IntelliJ автоматически распознает Maven проект

Шаг 2: Настройка SDK
File → Project Structure (Ctrl+Alt+Shift+S)

Project → SDK → выбери 21 (или скачай, если нет)

Project → Language Level → выбери 21 - Sealed types, always-strict floating-point semantics

Шаг 3: Создание конфигурации запуска
Run → Edit Configurations

Нажми + → Application

Заполни поля:

Name: TaskManager

Main class: com.taskmanager.TaskManagerApp

VM options: --add-modules javafx.controls,javafx.fxml,javafx.graphics

Working directory: $ProjectFileDir$

Use classpath of module: TaskManager

Нажми OK

Шаг 4: Запуск
Нажми Run (Shift+F10)

Или нажми зелёную кнопку ▶️ рядом с конфигурацией

Вариант 3: Командная строка с JAR файлом
Шаг 1: Создай исполняемый JAR
bash
mvn clean package
Шаг 2: Запусти JAR
bash
java --add-modules javafx.controls,javafx.fxml,javafx.graphics \
-jar target/TaskManager-1.5.0.jar
Для Windows:

powershell
java --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar target/TaskManager-1.5.0.jar
9. Успешный запуск
   При успешном запуске ты увидишь:
   ✅ Окно приложения откроется с интерфейсом:

Левая панель: форма создания задачи

Правая панель: таблица задач + оповещения

✅ В консоли логи:

text
2026-01-08 02:30:00 INFO  TaskManagerApp - Starting TaskManager...
2026-01-08 02:30:01 INFO  DatabaseConfig - Connected to PostgreSQL
2026-01-08 02:30:02 INFO  TaskService - TaskService initialized
2026-01-08 02:30:03 INFO  MainController - UI loaded successfully
✅ Таблица задач:

Если БД пустая — таблица пустая

Если есть задачи — они отображаются

✅ Счётчик оповещений внизу справа

10. Проверка работоспособности
    Тест 1: Создание задачи
    В поле "Описание" введи: Тестовая задача

В поле "Дата выполнения" введи: 0901 (нажми Enter)

Должно автозаполниться до 09.01.2026 00:00

Нажми "Создать задачу"

Должно появиться окно: "Задача создана: Тестовая задача"

Задача появится в таблице

Тест 2: Редактирование задачи
Двойной клик на созданной задаче

Откроется окно редактирования

Измени статус на IN_PROGRESS

Нажми "Сохранить"

Статус в таблице обновится

Тест 3: Смена темы
Нажми кнопку 🌙 в правом верхнем углу

Интерфейс должен стать тёмным

Кнопка изменится на ☀

Нажми ещё раз → вернётся светлая тема

Тест 4: Проверка БД
bash
psql -U postgres -d taskmanager

SELECT * FROM tasks;
Должна появиться строка с твоей тестовой задачей:

text
id |      title       |   description    |     due_date      | status | priority
----+------------------+------------------+-------------------+--------+----------
1 | Тестовая задача  | Тестовая задача  | 2026-01-09 00:00  | NEW    | 5
Выход из psql:

sql
\q
11. Типовые проблемы и решения
    ❌ «Cannot connect to database»
    Причины:

PostgreSQL не запущен

Неверные учётные данные в application.properties

БД taskmanager не существует

Решение:

bash
# Проверка запущен ли PostgreSQL

# Windows
tasklist | findstr postgres

# macOS/Linux
ps aux | grep postgres

# Если не запущен, запусти:
# Windows: Services → PostgreSQL → Start
# macOS: brew services start postgresql@15
# Linux: sudo service postgresql start

# Проверка существования БД
psql -U postgres -l | grep taskmanager

# Если нет БД — создай:
psql -U postgres -c "CREATE DATABASE taskmanager;"
❌ «relation "tasks" does not exist»
Причина: Таблицы не созданы в БД

Решение 1 (автоматически):

Убедись, что в application.properties:

text
spring.jpa.hibernate.ddl-auto=update
Запусти приложение один раз → Hibernate создаст таблицы

Решение 2 (вручную):

bash
psql -U postgres -d taskmanager -f src/main/resources/db/schema.sql
❌ «JavaFX runtime components are missing»
Причины:

Используешь JRE вместо JDK

Неправильная версия Java (не 21)

Отсутствуют JavaFX модули

Решение:

bash
# Проверка версии Java
java -version

# Должна быть "openjdk version 21..." или "java version 21..."
# Если показывает JRE, переустанови JDK 21

# Для Maven используй:
mvn javafx:run  # плагин автоматически настраивает модули

# Для IDE укажи VM options:
--add-modules javafx.controls,javafx.fxml,javafx.graphics
Скачать JDK 21:

Oracle: https://www.oracle.com/java/technologies/downloads/#java21

OpenJDK: https://jdk.java.net/21/

❌ «No visible @SpringBootApplication class»
Причина: Spring не находит класс TaskManagerApp

Решение:

bash
# Перекомпилируй проект
mvn clean compile

# Или в IntelliJ:
# File → Invalidate Caches / Restart
❌ Автозаполнение даты не работает
Симптомы:

Ввожу 0812, ничего не происходит

Причина: Используешь старую версию MainController.java

Решение:

Убедись, что в MainController.java есть метод autoFillDateTime()

Убедись, что в setupDateTimeInputMask() есть listeners:

java
dueDateTimeInput.focusedProperty().addListener(...)
dueDateTimeInput.setOnKeyPressed(...)
Пересобери проект: mvn clean install

Перезапусти приложение

❌ Тёмная тема не работает во всплывающем окне
Симптом: При двойном клике на задаче окно остаётся светлым, хотя основное окно тёмное

Причина: Используешь старую версию MainController.java

Решение:

Обновлён в версии 1.5.0. Убедись, что в методе openTaskDetailWindow() есть:

java
if (isDarkTheme) {
mainVBox.setStyle("...");
scrollPane.setStyle("...");
scene.setFill(Color.web("#1e1e1e"));
}
❌ «Port 8080 already in use»
Причина: Другое приложение занимает порт 8080

Решение 1: Измени порт в application.properties

text
server.port=8081
Решение 2: Останови процесс на порту 8080

bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# macOS/Linux
lsof -ti:8080 | xargs kill -9
12. Полезные команды
    Maven
    bash
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
PostgreSQL
bash
# Подключение к БД
psql -U postgres -d taskmanager

# Список таблиц
\dt

# Описание таблицы
\d tasks

# Вывод всех задач
SELECT * FROM tasks;

# Вывод задач с приоритетом > 5
SELECT * FROM tasks WHERE priority > 5;

# Удалить все задачи (осторожно!)
TRUNCATE TABLE tasks CASCADE;

# Выход
\q
Git
bash
# Проверка статуса
git status

# Добавить изменения
git add .

# Создать коммит
git commit -m "feat: описание изменений"

# Отправить в репозиторий
git push origin main

# Создать новую ветку
git checkout -b feature/my-feature

# Переключиться на ветку
git checkout main

# Обновить локальную копию
git pull origin main
13. Разработка
    Горячая перезагрузка (Spring Boot DevTools)
    Добавь в pom.xml:

xml
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-devtools</artifactId>
<scope>runtime</scope>
<optional>true</optional>
</dependency>
После сборки (Ctrl+F9 в IntelliJ) приложение автоматически перезагрузится.

Тестирование
bash
# Запуск всех тестов
mvn test

# Запуск конкретного теста
mvn test -Dtest=TaskServiceTest

# Тесты с отчётом покрытия
mvn test jacoco:report
14. Дополнительные ресурсы
    JavaFX: https://openjfx.io/

Spring Boot: https://spring.io/projects/spring-boot

Hibernate: https://hibernate.org/

PostgreSQL: https://www.postgresql.org/docs/

Maven: https://maven.apache.org/guides/

Версия документа: 1.5.0
Обновлено: 08.01.2026
Статус: ✅ ГОТОВО К ИСПОЛЬЗОВАНИЮ