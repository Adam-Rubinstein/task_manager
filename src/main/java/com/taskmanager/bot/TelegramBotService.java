package com.taskmanager.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * TelegramBotService - Telegram бот на Java (ФАЗА 2 опциональный вариант)
 * 
 * Альтернатива Python боту
 * 
 * Функции:
 * - Прослушивание сообщений от пользователей
 * - Парсинг команд (/start, /today, /list, /stats, /search)
 * - Отправка HTTP запросов на REST API (/api/voice/create-task)
 * - Форматирование ответов с emoji
 * 
 * Примеры команд:
 * /start             - приветствие
 * /today             - задачи на сегодня
 * /list              - все задачи
 * /stats             - статистика
 * /search молоко     - поиск по слову
 * Купить молоко      - создать задачу
 */
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TelegramBotService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            User user = update.getMessage().getFrom();

            try {
                if (messageText.startsWith("/")) {
                    // Команды
                    handleCommand(chatId, messageText, user);
                } else {
                    // Создание задачи из текста
                    handleVoiceMessage(chatId, messageText, user);
                }
            } catch (TelegramApiException e) {
                System.err.println("Ошибка отправки сообщения: " + e.getMessage());
            }
        }
    }

    /**
     * Обработка команд (/start, /today, /list и т.д.)
     */
    private void handleCommand(long chatId, String command, User user) throws TelegramApiException {
        String response = "";

        if (command.equals("/start")) {
            response = "👋 Привет! Я Voice Task Manager.\\n\\n" +
                    "Я помогу тебе управлять задачами через текст\\n\\n" +
                    "Команды:\\n" +
                    "/today - задачи на сегодня\\n" +
                    "/list - все задачи\\n" +
                    "/stats - статистика\\n" +
                    "/search [слово] - поиск\\n\\n" +
                    "Или просто напиши задачу:\\n" +
                    "«Купить молоко завтра в 15:00, приоритет 8»";

        } else if (command.equals("/today")) {
            response = "📅 Задачи на сегодня:\\n[Будет запрос к API /api/voice/today]";

        } else if (command.equals("/list")) {
            response = "📋 Последние задачи:\\n[Будет запрос к API /api/voice/list]";

        } else if (command.equals("/stats")) {
            response = "📊 Статистика:\\n[Будет запрос к API /api/voice/stats]";

        } else if (command.startsWith("/search")) {
            String keyword = command.replace("/search", "").trim();
            if (keyword.isEmpty()) {
                response = "❌ Укажи ключевое слово для поиска";
            } else {
                response = "🔍 Поиск по: " + keyword + "\\n[Будет запрос к API]";
            }

        } else {
            response = "❓ Неизвестная команда. Используй /start для справки";
        }

        sendMessage(chatId, response);
    }

    /**
     * Обработка голосового сообщения (создание задачи)
     */
    private void handleVoiceMessage(long chatId, String messageText, User user) throws TelegramApiException {
        try {
            // Подготовка запроса
            Map<String, Object> request = new HashMap<>();
            request.put("text", messageText);
            request.put("telegramUserId", user.getId());

            // Отправка на API
            String url = "http://localhost:8080/api/voice/create-task";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            try {
                Map response = restTemplate.postForObject(url, entity, Map.class);
                
                if (response != null && (Boolean) response.get("success")) {
                    String message = (String) response.get("message");
                    sendMessage(chatId, message);
                } else {
                    String error = response != null ? 
                            (String) response.get("error") : 
                            "Неизвестная ошибка";
                    sendMessage(chatId, "❌ " + error);
                }
            } catch (Exception e) {
                sendMessage(chatId, "❌ Ошибка: " + e.getMessage());
            }

        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка обработки сообщения");
        }
    }

    /**
     * Отправка сообщения в Telegram
     */
    private void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.enableMarkdownV2(false);

        execute(message);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}