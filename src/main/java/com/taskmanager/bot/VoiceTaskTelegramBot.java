package com.taskmanager.bot;

import com.taskmanager.dto.VoiceTaskParsed;
import com.taskmanager.model.Task;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.VoiceParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * VoiceTaskTelegramBot - Telegram бот для управления задачами
 * Принимает текстовые сообщения и создает задачи в БД
 *
 * Поддерживаемые команды:
 * /start - приветствие
 * /stats - статистика
 * /list - список последних задач
 * /today - задачи на сегодня
 * /help - справка
 *
 * Обычный текст → создание новой задачи
 */
@Component
public class VoiceTaskTelegramBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(VoiceTaskTelegramBot.class);

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Autowired
    private TaskService taskService;

    @Autowired
    private VoiceParsingService voiceParsingService;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update);
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", e.getMessage(), e);
        }
    }

    /**
     * Основной обработчик сообщений
     * Маршрутизирует команды и текст задач
     */
    private void handleMessage(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        log.info("📨 Получено сообщение от {}: {}", chatId, text);

        if (text.startsWith("/")) {
            handleCommand(chatId, text);
        } else {
            handleTaskCreation(chatId, text);
        }
    }

    /**
     * Обработка команд бота
     */
    private void handleCommand(long chatId, String command) throws TelegramApiException {
        String response;
        String cmd = command.toLowerCase();

        if (cmd.equals("/start")) {
            response = "👋 Привет! Я бот для управления задачами.\n\n" +
                    "📝 Отправь мне текст задачи:\n" +
                    "• Купить молоко завтра в 15:00, приоритет 8\n" +
                    "• Встреча через 3 дня\n" +
                    "• Отчет срочный\n\n" +
                    "⚙️ Команды:\n" +
                    "/stats - статистика\n" +
                    "/list - список задач\n" +
                    "/today - задачи на сегодня\n" +
                    "/help - справка";

        } else if (cmd.equals("/stats")) {
            response = getStatistics();

        } else if (cmd.equals("/list")) {
            response = getTasksList();

        } else if (cmd.equals("/today")) {
            response = getTodayTasks();

        } else if (cmd.equals("/help")) {
            response = "📋 Справка:\n\n" +
                    "✏️ Отправь задачу в формате:\n" +
                    "'Текст задачи [завтра/через N дней] [в HH:MM], [приоритет N]'\n\n" +
                    "📌 Примеры:\n" +
                    "• Купить молоко\n" +
                    "• Встреча завтра в 15:00\n" +
                    "• Отчет через 3 дня, приоритет 8\n" +
                    "• Срочное совещание завтра в 10:00, приоритет 9\n\n" +
                    "Все просто! 😊";

        } else {
            response = "❓ Неизвестная команда. Введи /help для справки";
        }

        sendMessage(chatId, response);
    }

    /**
     * Создание новой задачи из текста
     */
    private void handleTaskCreation(long chatId, String text) throws TelegramApiException {
        try {
            // Парсим текст задачи
            VoiceTaskParsed parsed = voiceParsingService.parseVoiceText(text);

            if (parsed == null || !voiceParsingService.isValidParsed(parsed)) {
                sendMessage(chatId, "❌ Не удалось распарсить текст. Попробуй снова.\n\nПример: 'Купить молоко завтра в 15:00'");
                return;
            }

            // Создаём задачу в БД
            Task task = taskService.createTaskFromVoice(parsed);

            if (task == null) {
                sendMessage(chatId, "❌ Не удалось создать задачу. Попробуй снова.");
                return;
            }

            // Отправляем результат пользователю
            String response = formatTaskResponse(task);
            sendMessage(chatId, response);
            log.info("✅ Задача создана: '{}' (ID: {}, Приоритет: {})",
                    task.getTitle(), task.getId(), task.getPriority());

        } catch (Exception e) {
            log.error("❌ Ошибка при создании задачи: {}", e.getMessage(), e);
            sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
        }
    }

    /**
     * Получить статистику по задачам
     */
    private String getStatistics() {
        try {
            long total = taskService.getTotalTaskCount();
            long active = taskService.getActiveTaskCount();
            long completed = taskService.getCompletedTaskCount();
            long overdue = taskService.getOverdueTaskCount();

            return String.format(
                    "📊 Статистика:\n\n" +
                            "📈 Всего задач: %d\n" +
                            "🔵 Активных: %d\n" +
                            "✅ Завершено: %d\n" +
                            "⚠️ Просроченных: %d",
                    total, active, completed, overdue
            );
        } catch (Exception e) {
            log.error("❌ Ошибка при получении статистики: {}", e.getMessage());
            return "❌ Ошибка при получении статистики";
        }
    }

    /**
     * Получить список последних 5 задач
     */
    private String getTasksList() {
        try {
            List<Task> tasks = taskService.getLatestTasks(5);

            if (tasks.isEmpty()) {
                return "📭 Задач нет";
            }

            StringBuilder sb = new StringBuilder("📋 Последние 5 задач:\n\n");
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                sb.append(String.format("%d. %s\n", i + 1, task.getTitle()));

                if (task.getDueDate() != null) {
                    sb.append("   📅 ").append(formatDate(task.getDueDate())).append("\n");
                }

                sb.append("   🔴 Приоритет: ").append(task.getPriority());

                if (task.getIsUrgent() != null && task.getIsUrgent()) {
                    sb.append(" ⚡ СРОЧНО");
                }

                sb.append("\n   📌 Статус: ").append(task.getStatus()).append("\n\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("❌ Ошибка при получении списка: {}", e.getMessage());
            return "❌ Ошибка при получении списка";
        }
    }

    /**
     * Получить задачи на сегодня
     */
    private String getTodayTasks() {
        try {
            List<Task> tasks = taskService.getTasksForToday();

            if (tasks.isEmpty()) {
                return "✅ Задач на сегодня нет";
            }

            StringBuilder sb = new StringBuilder(String.format("📅 Задачи на сегодня (%d):\n\n", tasks.size()));
            int index = 1;

            for (Task task : tasks) {
                sb.append(index++).append(". ").append(task.getTitle()).append("\n");

                if (task.getDueDate() != null) {
                    sb.append("   ⏰ ").append(formatTime(task.getDueDate())).append("\n");
                }

                sb.append("   🔴 Приоритет: ").append(task.getPriority());

                if (task.getIsUrgent() != null && task.getIsUrgent()) {
                    sb.append(" ⚡");
                }

                sb.append("\n\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("❌ Ошибка при получении задач на сегодня: {}", e.getMessage());
            return "❌ Ошибка при получении задач";
        }
    }

    /**
     * Форматировать ответ о созданной задаче
     */
    private String formatTaskResponse(Task task) {
        StringBuilder response = new StringBuilder();
        response.append("✅ Задача создана!\n\n");
        response.append("📝 ").append(task.getTitle()).append("\n");

        if (task.getDueDate() != null) {
            response.append("📅 Срок: ").append(formatDate(task.getDueDate())).append("\n");
        } else {
            response.append("📅 Срок: не установлена\n");
        }

        response.append("🔴 Приоритет: ").append(task.getPriority());

        if (task.getIsUrgent() != null && task.getIsUrgent()) {
            response.append(" ⚡ СРОЧНО");
        }

        response.append("\n");
        response.append("🆔 ID: ").append(task.getId()).append("\n");
        response.append("📌 Статус: ").append(task.getStatus());

        return response.toString();
    }

    /**
     * Форматировать дату с временем (dd.MM.yyyy HH:mm)
     */
    private String formatDate(LocalDateTime date) {
        if (date == null) return "не установлена";
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    /**
     * Форматировать время (HH:mm)
     */
    private String formatTime(LocalDateTime date) {
        if (date == null) return "не установлено";
        return date.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Отправить сообщение в Telegram
     */
    private void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        execute(message);
        log.debug("📤 Сообщение отправлено в чат {}", chatId);
    }
}