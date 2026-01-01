package com.taskmanager.dto;

/**
 * VoiceTaskRequest - DTO для запроса от Telegram бота
 * 
 * Структура:
 * {
 *   "text": "Купить молоко завтра в 15:00, приоритет 8",
 *   "telegramUserId": 123456789
 * }
 */
public class VoiceTaskRequest {

    private String text;
    private Long telegramUserId;

    // Constructors
    public VoiceTaskRequest() {
    }

    public VoiceTaskRequest(String text, Long telegramUserId) {
        this.text = text;
        this.telegramUserId = telegramUserId;
    }

    // Getters и Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(Long telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    @Override
    public String toString() {
        return "VoiceTaskRequest{" +
                "text='" + text + '\'' +
                ", telegramUserId=" + telegramUserId +
                '}';
    }
}