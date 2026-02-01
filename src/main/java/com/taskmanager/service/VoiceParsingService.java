package com.taskmanager.service;

import com.taskmanager.dto.VoiceTaskParsed;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VoiceParsingService - Парсинг голосового текста
 */
@Service
public class VoiceParsingService {

    private static final Logger log = LoggerFactory.getLogger(VoiceParsingService.class);

    // Форматтер даты
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Константы для приоритета
    private static final int DEFAULT_PRIORITY = 5;
    private static final int MIN_PRIORITY = 0;
    private static final int MAX_PRIORITY = 10;
    private static final int URGENT_PRIORITY_THRESHOLD = 7;

    // Константы для времени по умолчанию
    private static final int DEFAULT_HOUR = 9;
    private static final int DEFAULT_MINUTE = 0;

    // Константы для рекуррентности
    private static final int DEFAULT_RECURRENCE_DAYS = 7;

    // Регулярные выражения
    private static final String PRIORITY_PATTERN_RU = "(приоритет|важность)[\\s:]*([0-9]+)";
    private static final String PRIORITY_PATTERN_EN = "priority[\\s:]*([0-9]+)";
    private static final String TIME_PATTERN = "в\\s+(\\d{1,2}):(\\d{2})";
    private static final String DAYS_PATTERN = "через\\s+(\\d+)\\s+(дн[яе]?)";

    // Ключевые слова для парсинга
    private static final String KEYWORD_TOMORROW = "завтра";
    private static final String KEYWORD_URGENT_RU = "срочно";
    private static final String KEYWORD_IMMEDIATELY = "немедленно";
    private static final String KEYWORD_CRITICAL = "критично";
    private static final String KEYWORD_EMERGENCY = "emergency";
    private static final String KEYWORD_URGENT_EN = "urgent";

    /**
     * Основной метод парсинга голосового текста
     */
    public VoiceTaskParsed parseVoiceText(String text) {
        if (text == null || text.isEmpty()) {
            log.warn("Попытка распарсить пустой текст");
            return null;
        }

        log.debug("Парсинг голосового текста: '{}'", text);
        VoiceTaskParsed parsed = new VoiceTaskParsed();

        // 1. Парсинг приоритета
        Integer priority = parsePriority(text);
        parsed.setPriority(priority != null ? priority : DEFAULT_PRIORITY);
        log.debug("Приоритет: {}", parsed.getPriority());

        // 2. Парсинг даты
        LocalDateTime dueDate = parseDate(text);
        parsed.setDueDate(dueDate);
        log.debug("Дата выполнения: {}", dueDate);

        // 3. Очистка текста
        String cleanedTitle = cleanText(text);
        parsed.setTitle(cleanedTitle);
        log.debug("Очищенный текст: '{}'", cleanedTitle);

        // 4. Description
        parsed.setDescription(text);

        // 5. Проверка срочности
        boolean isUrgent = isUrgent(text, priority);
        parsed.setIsUrgent(isUrgent);
        log.debug("Срочность: {}", isUrgent);

        log.info("Голосовой текст распарсен: title='{}', priority={}, urgent={}",
                cleanedTitle, parsed.getPriority(), isUrgent);

        return parsed;
    }

    /**
     * Парсинг приоритета из текста
     * Ищет: "приоритет 8", "важность 7", "priority 5"
     */
    private Integer parsePriority(String text) {
        if (text == null) return null;

        // Русские варианты
        Pattern pattern = Pattern.compile(PRIORITY_PATTERN_RU, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                int priority = Integer.parseInt(matcher.group(2));
                int result = Math.min(MAX_PRIORITY, Math.max(MIN_PRIORITY, priority));
                log.debug("Найден приоритет (рус): {}", result);
                return result;
            } catch (NumberFormatException e) {
                log.warn("Ошибка парсинга приоритета: {}", matcher.group(2));
                return null;
            }
        }

        // English
        pattern = Pattern.compile(PRIORITY_PATTERN_EN, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                int priority = Integer.parseInt(matcher.group(1));
                int result = Math.min(MAX_PRIORITY, Math.max(MIN_PRIORITY, priority));
                log.debug("Найден приоритет (eng): {}", result);
                return result;
            } catch (NumberFormatException e) {
                log.warn("Ошибка парсинга приоритета: {}", matcher.group(1));
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

        // "завтра"
        if (text.contains(KEYWORD_TOMORROW)) {
            LocalDateTime tomorrow = now.plusDays(1);
            Pattern timePattern = Pattern.compile(TIME_PATTERN);
            Matcher timeMatcher = timePattern.matcher(text);
            if (timeMatcher.find()) {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = Integer.parseInt(timeMatcher.group(2));
                LocalDateTime result = tomorrow.withHour(hour).withMinute(minute).withSecond(0);
                log.debug("Найдена дата: завтра в {}:{}", hour, minute);
                return result;
            }
            log.debug("Найдена дата: завтра {}:{}",DEFAULT_HOUR, DEFAULT_MINUTE);
            return tomorrow.withHour(DEFAULT_HOUR).withMinute(DEFAULT_MINUTE).withSecond(0);
        }

        // "через N дней"
        Pattern daysPattern = Pattern.compile(DAYS_PATTERN);
        Matcher daysMatcher = daysPattern.matcher(text);
        if (daysMatcher.find()) {
            int days = Integer.parseInt(daysMatcher.group(1));
            LocalDateTime future = now.plusDays(days);
            Pattern timePattern = Pattern.compile(TIME_PATTERN);
            Matcher timeMatcher = timePattern.matcher(text);
            if (timeMatcher.find()) {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = Integer.parseInt(timeMatcher.group(2));
                log.debug("Найдена дата: через {} дней в {}:{}", days, hour, minute);
                return future.withHour(hour).withMinute(minute).withSecond(0);
            }
            log.debug("Найдена дата: через {} дней в {}:{}", days, DEFAULT_HOUR, DEFAULT_MINUTE);
            return future.withHour(DEFAULT_HOUR).withMinute(DEFAULT_MINUTE).withSecond(0);
        }

        // "в N:MM"
        Pattern timePattern = Pattern.compile(TIME_PATTERN);
        Matcher timeMatcher = timePattern.matcher(text);
        if (timeMatcher.find()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int minute = Integer.parseInt(timeMatcher.group(2));
            log.debug("Найдено время: {}:{}", hour, minute);
            return now.withHour(hour).withMinute(minute).withSecond(0);
        }

        return null;
    }

    /**
     * Очистить текст от дат и приоритета
     */
    public String cleanText(String text) {
        if (text == null) return "";

        log.debug("Очистка текста: '{}'", text);

        // Убрать приоритет
        text = text.replaceAll(PRIORITY_PATTERN_RU, "").trim();
        text = text.replaceAll(PRIORITY_PATTERN_EN, "").trim();

        // Убрать дату
        text = text.replaceAll(KEYWORD_TOMORROW, "").trim();
        text = text.replaceAll(DAYS_PATTERN, "").trim();
        text = text.replaceAll(TIME_PATTERN, "").trim();
        text = text.replaceAll("в\\s+\\d{1,2}\\s+часов", "").trim();

        // Убрать лишние запятые и пробелы
        text = text.replaceAll(",\\s*$", "").trim();
        text = text.replaceAll("\\s+", " ").trim();

        log.debug("Результат очистки: '{}'", text);
        return text;
    }

    /**
     * Определить срочность задачи
     */
    private boolean isUrgent(String text, Integer priority) {
        if (text == null) return false;

        text = text.toLowerCase();

        // Высокий приоритет
        if (priority != null && priority >= URGENT_PRIORITY_THRESHOLD) {
            log.debug("Задача срочная (приоритет >= {})", URGENT_PRIORITY_THRESHOLD);
            return true;
        }

        // Ключевые слова срочности
        boolean urgent = text.contains(KEYWORD_URGENT_RU) ||
                text.contains(KEYWORD_IMMEDIATELY) ||
                text.contains(KEYWORD_CRITICAL) ||
                text.contains(KEYWORD_EMERGENCY) ||
                text.contains(KEYWORD_URGENT_EN);

        if (urgent) {
            log.debug("Задача срочная (ключевые слова)");
        }

        return urgent;
    }

    /**
     * Проверить валидность распарсенных данных
     */
    public boolean isValidParsed(VoiceTaskParsed parsed) {
        boolean valid = parsed != null &&
                parsed.getTitle() != null &&
                !parsed.getTitle().isEmpty();

        if (!valid) {
            log.warn("Некорректно распарсенные данные: {}", parsed);
        }

        return valid;
    }
}
