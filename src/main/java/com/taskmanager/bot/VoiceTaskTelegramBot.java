package com.taskmanager.bot;

import com.taskmanager.dto.VoiceTaskParsed;
import com.taskmanager.model.Task;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.VoiceParsingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * VoiceTaskTelegramBot - Telegram бот для голосовых задач
 * 
 * Функции:
 * - Получение голосовых сообщений
 * - Распознавание текста (речь -> текст)
 * - Парсинг дата, приоритета из текста
 * - Создание задач
 * - Отправка результата в Telegram
 */
@Slf4j
@Component
public class VoiceTaskTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Autowired
    private TaskService taskService;

    @Autowired
    private VoiceParsingService voiceParsingService;

    @Autowired
    private SpeechRecognitionService speechRecognitionService;

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
            // Обработка текстовых сообщений
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);
            }
            // Обработка голосовых сообщений
            else if (update.hasMessage() && update.getMessage().hasVoice()) {
                handleVoiceMessage(update);
            }
            // Обработка команд
            else if (update.hasMessage() && update.getMessage().isCommand()) {
                handleCommand(update);
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке update: {}", e.getMessage(), e);
            sendErrorMessage(update.getMessage().getChatId(), "❌ Ошибка обработки: " + e.getMessage());
        }
    }

    /**
     * Обработка текстового сообщения
     */
    private void handleTextMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        long userId = update.getMessage().getFrom().getId();

        log.info("📝 Текстовое сообщение от {}: {}", userId, text);

        // Парсим текст
        VoiceTaskParsed parsed = voiceParsingService.parseVoiceText(text);
        
        if (parsed == null || !voiceParsingService.isValidParsed(parsed)) {
            sendMessage(chatId, "❌ Не удалось распарсить текст. Попробуй снова.");
            return;
        }

        // Создаём задачу
        Task task = taskService.createTaskFromVoice(parsed);
        
        if (task == null) {
            sendMessage(chatId, "❌ Не удалось создать задачу.");
            return;
        }

        // Отправляем результат
        String responseMessage = formatTaskMessage(task);
        sendMessage(chatId, responseMessage);
        
        log.info("✅ Задача создана: {} (ID: {})", task.getTitle(), task.getId());
    }

    /**
     * Обработка голосового сообщения
     */
    private void handleVoiceMessage(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        Voice voice = update.getMessage().getVoice();
        long userId = update.getMessage().getFrom().getId();

        log.info("🎤 Голосовое сообщение от {}, длительность: {}s", userId, voice.getDuration());

        // Отправляем "печатает..."
        sendMessage(chatId, "⏳ Обрабатываю голосовое сообщение...");

        try {
            // Скачиваем файл с Telegram серверов
            String filePath = downloadVoiceFile(voice.getFileId());
            
            // Распознаём речь -> текст
            String recognizedText = speechRecognitionService.recognizeSpeech(filePath);
            
            if (recognizedText == null || recognizedText.isEmpty()) {
                sendMessage(chatId, "❌ Не удалось распознать речь. Попробуй снова.");
                return;
            }

            log.info("🎯 Распознанный текст: {}", recognizedText);

            // Парсим текст (извлекаем дату, приоритет)
            VoiceTaskParsed parsed = voiceParsingService.parseVoiceText(recognizedText);
            
            if (parsed == null || !voiceParsingService.isValidParsed(parsed)) {
                sendMessage(chatId, "❌ Не удалось распарсить текст: " + recognizedText);
                return;
            }

            // Создаём задачу
            Task task = taskService.createTaskFromVoice(parsed);
            
            if (task == null) {
                sendMessage(chatId, "❌ Не удалось создать задачу.");
                return;
            }

            // Отправляем результат с распознанным текстом
            String responseMessage = String.format(
                "✅ Задача создана!\\n\\n" +
                "🎯 Распознано: %s\\n" +
                "📝 Задача: %s\\n" +
                "📅 Срок: %s\\n" +
                "🔴 Приоритет: %d%s",
                recognizedText,
                task.getTitle(),
                formatDate(task.getDueDate()),
                task.getPriority(),
                task.getIsUrgent() ? " ⚡ СРОЧНО" : ""
            );
            
            sendMessage(chatId, responseMessage);
            
            log.info("✅ Голосовая задача создана: {} (ID: {})", task.getTitle(), task.getId());

        } catch (Exception e) {
            log.error("Ошибка при обработке голоса: {}", e.getMessage(), e);
            sendMessage(chatId, "❌ Ошибка при обработке голоса: " + e.getMessage());
        }
    }

    /**
     * Обработка команд (/start, /stats, /list, etc.)
     */
    private void handleCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        String command = update.getMessage().getText();

        log.info("⚙️ Команда: {}", command);

        switch (command.toLowerCase()) {
            case "/start":
                sendMessage(chatId, 
                    "👋 Привет! Я бот для управления задачами.\\n\\n" +
                    "Я могу:\\n" +
                    "🎤 Распознавать голосовые сообщения\\n" +
                    "📝 Создавать задачи из текста\\n" +
                    "📅 Парсить даты и приоритеты\\n\\n" +
                    "Команды:\\n" +
                    "/stats - статистика задач\\n" +
                    "/list - список задач\\n" +
                    "/today - задачи на сегодня\\n" +
                    "/overdue - просроченные задачи\\n" +
                    "/help - справка"
                );
                break;

            case "/stats":
                sendStatsMessage(chatId);
                break;

            case "/list":
                sendListMessage(chatId);
                break;

            case "/today":
                sendTodayMessage(chatId);
                break;

            case "/overdue":
                sendOverdueMessage(chatId);
                break;

            case "/help":
                sendMessage(chatId,
                    "📋 Справка:\\n\\n" +
                    "Отправь голосовое или текстовое сообщение с описанием задачи:\\n\\n" +
                    "Примеры:\\n" +
                    "• Купить молоко завтра в 15:00, приоритет 8\\n" +
                    "• Подготовить отчет через 3 дня, срочно\\n" +
                    "• Встреча в 14:00 сегодня, важность 7\\n\\n" +
                    "Я распознаю:\\n" +
                    "📅 Даты: завтра, через N дней, в HH:MM\\n" +
                    "🔴 Приоритет: 0-10 (по умолчанию 5)\\n" +
                    "⚡ Срочность: слова 'срочно', 'немедленно', etc."
                );
                break;

            default:
                sendMessage(chatId, "❓ Неизвестная команда. Введи /help для справки.");
        }
    }

    /**
     * Отправить статистику
     */
    private void sendStatsMessage(long chatId) {
        try {
            long total = taskService.getTotalTaskCount();
            long active = taskService.getActiveTaskCount();
            long completed = taskService.getCompletedTaskCount();
            long overdue = taskService.getOverdueTaskCount();

            String stats = String.format(
                "📊 Статистика задач:\\n\\n" +
                "📈 Всего: %d\\n" +
                "🔵 Активных: %d\\n" +
                "✅ Завершено: %d\\n" +
                "⚠️ Просроченных: %d",
                total, active, completed, overdue
            );
            sendMessage(chatId, stats);
        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка при получении статистики");
        }
    }

    /**
     * Отправить список задач
     */
    private void sendListMessage(long chatId) {
        try {
            var tasks = taskService.getLatestTasks(5);
            
            if (tasks.isEmpty()) {
                sendMessage(chatId, "📭 Задач нет");
                return;
            }

            StringBuilder sb = new StringBuilder("📋 Последние 5 задач:\\n\\n");
            for (Task task : tasks) {
                sb.append(formatTaskBrief(task)).append("\\n");
            }

            sendMessage(chatId, sb.toString());
        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка при получении списка");
        }
    }

    /**
     * Отправить задачи на сегодня
     */
    private void sendTodayMessage(long chatId) {
        try {
            var tasks = taskService.getTasksForToday();
            
            if (tasks.isEmpty()) {
                sendMessage(chatId, "✅ Задач на сегодня нет");
                return;
            }

            StringBuilder sb = new StringBuilder(String.format("📅 Задачи на сегодня (%d):\\n\\n", tasks.size()));
            for (Task task : tasks) {
                sb.append(formatTaskBrief(task)).append("\\n");
            }

            sendMessage(chatId, sb.toString());
        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка при получении задач");
        }
    }

    /**
     * Отправить просроченные задачи
     */
    private void sendOverdueMessage(long chatId) {
        try {
            var tasks = taskService.getOverdueTasks();
            
            if (tasks.isEmpty()) {
                sendMessage(chatId, "✅ Просроченных задач нет");
                return;
            }

            StringBuilder sb = new StringBuilder(String.format("⚠️ Просроченные задачи (%d):\\n\\n", tasks.size()));
            for (Task task : tasks) {
                sb.append(formatTaskBrief(task)).append("\\n");
            }

            sendMessage(chatId, sb.toString());
        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка при получении задач");
        }
    }

    /**
     * Форматировать сообщение о задаче (полное)
     */
    private String formatTaskMessage(Task task) {
        return String.format(
            "✅ Задача создана!\\n\\n" +
            "📝 %s\\n" +
            "📅 Срок: %s\\n" +
            "🔴 Приоритет: %d%s\\n" +
            "🆔 ID: %d",
            task.getTitle(),
            formatDate(task.getDueDate()),
            task.getPriority(),
            task.getIsUrgent() ? " ⚡ СРОЧНО" : "",
            task.getId()
        );
    }

    /**
     * Форматировать сообщение о задаче (краткое)
     */
    private String formatTaskBrief(Task task) {
        return String.format(
            "%s %s | Приоритет: %d%s",
            task.getTitle(),
            formatDate(task.getDueDate()),
            task.getPriority(),
            task.getIsUrgent() ? " ⚡" : ""
        );
    }

    /**
     * Форматировать дату
     */
    private String formatDate(LocalDateTime date) {
        if (date == null) return "не установлена";
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    /**
     * Скачать голосовой файл с Telegram серверов
     */
    private String downloadVoiceFile(String fileId) throws Exception {
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);
        
        var file = execute(getFile);
        String filePath = file.getFilePath();
        String downloadUrl = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;

        // Скачиваем файл
        URL url = new URL(downloadUrl);
        URLConnection conn = url.openConnection();
        
        File outputFile = new File("temp_voice_" + System.currentTimeMillis() + ".oga");
        
        try (InputStream in = conn.getInputStream();
             var out = new java.io.FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        log.info("📥 Файл скачан: {}", outputFile.getAbsolutePath());
        return outputFile.getAbsolutePath();
    }

    /**
     * Отправить сообщение в Telegram
     */
    private void sendMessage(long chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            message.enableMarkdown(true);
            
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage());
        }
    }

    /**
     * Отправить сообщение об ошибке
     */
    private void sendErrorMessage(long chatId, String error) {
        sendMessage(chatId, "❌ Ошибка: " + error + "\\n\\nПопробуй снова или введи /help");
    }
}
