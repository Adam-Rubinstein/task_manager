package com.taskmanager.config;

import com.taskmanager.bot.TelegramBotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * TelegramBotConfig - Spring конфигурация для Telegram бота (ФАЗА 2 опционально)
 * 
 * Активируется если в application.properties:
 * telegram.bot.enabled=true
 */
@Configuration
@ConditionalOnProperty(
        name = "telegram.bot.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class TelegramBotConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    /**
     * Bean для TelegramBotService
     */
    @Bean
    public TelegramBotService telegramBotService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        TelegramBotService service = new TelegramBotService(restTemplate, objectMapper);
        
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(service);
            System.out.println("✅ Telegram Bot зарегистрирован: @" + botUsername);
        } catch (Exception e) {
            System.err.println("❌ Ошибка при регистрации Telegram бота: " + e.getMessage());
            e.printStackTrace();
        }
        
        return service;
    }

    /**
     * Bean для RestTemplate (если его нет)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Bean для ObjectMapper (если его нет)
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}