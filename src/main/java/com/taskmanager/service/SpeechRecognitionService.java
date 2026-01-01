package com.taskmanager.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * SpeechRecognitionService - Распознавание речи (Speech-to-Text)
 * 
 * Варианты реализации:
 * 1. Google Cloud Speech-to-Text API (платный, но точный)
 * 2. Vosk (локальный, бесплатный)
 * 3. OpenAI Whisper (платный, очень точный)
 */
@Slf4j
@Service
public class SpeechRecognitionService {

    /**
     * Распознать речь из аудиофайла
     * 
     * Для простоты используем Google Cloud Speech API через curl
     * Для production используй:
     * - Google Cloud Speech-to-Text
     * - OpenAI Whisper API
     * - или локальный Vosk
     */
    public String recognizeSpeech(String audioFilePath) throws IOException {
        log.info("🎤 Распознаю речь из файла: {}", audioFilePath);

        try {
            // Проверить файл
            File audioFile = new File(audioFilePath);
            if (!audioFile.exists()) {
                log.error("❌ Файл не найден: {}", audioFilePath);
                return null;
            }

            // ВАРИАНТ 1: Использовать Google Cloud Speech API (требует API ключ)
            // return recognizeWithGoogleCloud(audioFilePath);

            // ВАРИАНТ 2: Использовать OpenAI Whisper (требует API ключ)
            // return recognizeWithOpenAIWhisper(audioFilePath);

            // ВАРИАНТ 3: Использовать локальный Vosk (бесплатный)
            return recognizeWithVosk(audioFilePath);

        } catch (Exception e) {
            log.error("❌ Ошибка распознавания речи: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Распознавание с помощью Vosk (локальный, бесплатный)
     * 
     * Установка Vosk:
     * 1. npm install -g vosk-server
     * 2. vosk-server -s model_ru_ru (русский язык)
     * 
     * Или скачай готовый образ Docker
     */
    private String recognizeWithVosk(String audioFilePath) throws IOException {
        log.info("🎤 Распознавание через Vosk...");

        try {
            // Запускаем ffmpeg для конвертирования в WAV (Vosk требует WAV)
            String wavFile = audioFilePath.replace(".oga", ".wav");
            
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", audioFilePath, "-acodec", "pcm_s16le", "-ar", "16000", wavFile
            );
            pb.start().waitFor();

            // Отправляем WAV на Vosk сервер (который слушает на localhost:2700)
            // Используем curl для простоты
            ProcessBuilder curlPb = new ProcessBuilder(
                "curl", "-X", "POST",
                "--data-binary", "@" + wavFile,
                "http://localhost:2700/speech",
                "-H", "Content-Type: audio/wav"
            );

            Process process = curlPb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            process.waitFor();

            // Парсим JSON ответ
            String response = result.toString();
            log.info("🎯 Ответ Vosk: {}", response);

            // Извлекаем текст из JSON: {"result": [{"conf": 1, "result": "...", ...}], "final": true}
            if (response.contains("\"result\"")) {
                // Простой парсинг
                int startIdx = response.lastIndexOf("\"result\":\"") + 10;
                int endIdx = response.indexOf("\"", startIdx);
                if (startIdx > 10 && endIdx > startIdx) {
                    return response.substring(startIdx, endIdx);
                }
            }

            return null;

        } catch (Exception e) {
            log.error("❌ Ошибка Vosk: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Распознавание с помощью Google Cloud Speech API
     * 
     * Требует:
     * 1. Создать учётную запись Google Cloud
     * 2. Включить Speech-to-Text API
     * 3. Скачать JSON ключ сервис-аккаунта
     * 4. Установить переменную окружения: GOOGLE_APPLICATION_CREDENTIALS
     */
    private String recognizeWithGoogleCloud(String audioFilePath) {
        log.info("🎤 Распознавание через Google Cloud...");
        
        // Требует google-cloud-speech зависимость
        // Реализация опускается для простоты
        
        return null;
    }

    /**
     * Распознавание с помощью OpenAI Whisper API
     * 
     * Требует:
     * 1. Создать аккаунт OpenAI
     * 2. Получить API ключ
     * 3. Отправить аудиофайл на Whisper API
     */
    private String recognizeWithOpenAIWhisper(String audioFilePath) {
        log.info("🎤 Распознавание через OpenAI Whisper...");
        
        // Требует API ключ и зависимость okhttp для запросов
        // Реализация опускается для простоты
        
        return null;
    }

    /**
     * Проверить, доступен ли Vosk сервер
     */
    public boolean isVoskAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "curl", "-s", "http://localhost:2700/status"
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
