package com.taskmanager.service;

import com.taskmanager.dto.VoiceTaskParsed;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VoiceParsingService - Парсинг голосового текста
 * ФАЗА 2: Извлечение дат, времени и приоритета из текста
 */
@Service
public class VoiceParsingService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Основной метод парсинга голосового текста
     */
    public VoiceTaskParsed parseVoiceText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        VoiceTaskParsed parsed = new VoiceTaskParsed();

        // 1. Парсинг приоритета
        Integer priority = parsePriority(text);
        parsed.setPriority(priority != null ? priority : 5);

        // 2. Парсинг даты
        LocalDateTime dueDate = parseDate(text);
        parsed.setDueDate(dueDate);

        // 3. Очистка текста (убрать дату и приоритет)
        String cleanedTitle = cleanText(text);
        parsed.setTitle(cleanedTitle);

        // 4. Description = исходный текст (можно доработать)
        parsed.setDescription(text);

        // 5. Проверка срочности
        boolean isUrgent = isUrgent(text, priority);
        parsed.setIsUrgent(isUrgent);

        return parsed;
    }

    /**
     * Парсинг приоритета из текста
     * Ищет: "приоритет 8", "важность 7", "priority 5"
     */
    private Integer parsePriority(String text) {
        if (text == null) return null;

        // Русские варианты
        Pattern pattern = Pattern.compile("(приоритет|важность)[\\s:]*([0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            try {
                int priority = Integer.parseInt(matcher.group(2));
                return Math.min(10, Math.max(0, priority)); // 0-10
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // English
        pattern = Pattern.compile("priority[\\s:]*([0-9]+)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(text);

        if (matcher.find()) {
            try {
                int priority = Integer.parseInt(matcher.group(1));
                return Math.min(10, Math.max(0, priority));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Парсинг даты из текста
     * Ищет: "завтра", "через 3 дня", "в 15:00", "в понедельник"
     */
    private LocalDateTime parseDate(String text) {
        if (text == null) return null;

        LocalDateTime now = LocalDateTime.now();
        text = text.toLowerCase();

        // "завтра" → +1 день
        if (text.contains("завтра")) {
            LocalDateTime tomorrow = now.plusDays(1);
            // Ищем время в формате "в 15:00"
            Pattern timePattern = Pattern.compile("в\\s+(\\d{1,2}):(\\d{2})");
            Matcher timeMatcher = timePattern.matcher(text);
            if (timeMatcher.find()) {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = Integer.parseInt(timeMatcher.group(2));
                return tomorrow.withHour(hour).withMinute(minute).withSecond(0);
            }
            return tomorrow.withHour(9).withMinute(0).withSecond(0);
        }

        // "через N дней" → +N дней
        Pattern daysPattern = Pattern.compile("через\\s+(\\d+)\\s+(дн[яе]?)");
        Matcher daysMatcher = daysPattern.matcher(text);
        if (daysMatcher.find()) {
            int days = Integer.parseInt(daysMatcher.group(1));
            LocalDateTime future = now.plusDays(days);
            // Ищем время
            Pattern timePattern = Pattern.compile("в\\s+(\\d{1,2}):(\\d{2})");
            Matcher timeMatcher = timePattern.matcher(text);
            if (timeMatcher.find()) {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = Integer.parseInt(timeMatcher.group(2));
                return future.withHour(hour).withMinute(minute).withSecond(0);
            }
            return future.withHour(9).withMinute(0).withSecond(0);
        }

        // "в N часов" или "в 15:00"
        Pattern timePattern = Pattern.compile("в\\s+(\\d{1,2}):(\\d{2})");
        Matcher timeMatcher = timePattern.matcher(text);
        if (timeMatcher.find()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int minute = Integer.parseInt(timeMatcher.group(2));
            return now.withHour(hour).withMinute(minute).withSecond(0);
        }

        return null;
    }

    /**
     * Очистить текст от дат и приоритета
     */
    public String cleanText(String text) {
        if (text == null) return "";

        // Убрать приоритет
        text = text.replaceAll("(приоритет|важность|priority)[\\s:]*[0-9]+", "").trim();

        // Убрать дату
        text = text.replaceAll("завтра", "").trim();
        text = text.replaceAll("через\\s+\\d+\\s+дн[яе]?", "").trim();
        text = text.replaceAll("в\\s+\\d{1,2}:\\d{2}", "").trim();
        text = text.replaceAll("в\\s+\\d{1,2}\\s+часов", "").trim();

        // Убрать лишние запятые и пробелы
        text = text.replaceAll(",\\s*$", "").trim();
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    /**
     * Определить срочность задачи
     */
    private boolean isUrgent(String text, Integer priority) {
        if (text == null) return false;

        text = text.toLowerCase();

        // Высокий приоритет
        if (priority != null && priority >= 7) {
            return true;
        }

        // Ключевые слова срочности
        return text.contains("срочно") ||
                text.contains("немедленно") ||
                text.contains("критично") ||
                text.contains("emergency") ||
                text.contains("urgent");
    }

    /**
     * Проверить валидность распарсенных данных
     */
    public boolean isValidParsed(VoiceTaskParsed parsed) {
        return parsed != null &&
                parsed.getTitle() != null &&
                !parsed.getTitle().isEmpty();
    }
}
