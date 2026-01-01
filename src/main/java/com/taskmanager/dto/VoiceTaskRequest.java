package com.taskmanager.dto;

public class VoiceTaskRequest {
    private String text;
    private Long telegramUserId;

    public VoiceTaskRequest() {}

    public VoiceTaskRequest(String text, Long telegramUserId) {
        this.text = text;
        this.telegramUserId = telegramUserId;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Long getTelegramUserId() { return telegramUserId; }
    public void setTelegramUserId(Long telegramUserId) { this.telegramUserId = telegramUserId; }
}
