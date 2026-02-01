package com.taskmanager.service;

import com.taskmanager.dto.VoiceTaskParsed;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VoiceParsingService {

    private static final Logger log = LoggerFactory.getLogger(VoiceParsingService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

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
        parsed.setPriority(priority != null ? priority : 5);
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
     */
    private Integer parsePriority(String text) {
        if (text == null) return null;

        // Русские варианты
        Pattern pattern = Pattern.compile("(приоритет|важность)[\\s:]*([0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                int priority = Integer.parseInt(matcher.group(2));
                int result = Math.min(10, Math.max(0, priority));
                log.debug("Найден приоритет (рус): {}", result);
                return result;
            } catch (NumberFormatException e) {
                log.warn("Ошибка парсинга приоритета: {}", matcher.group(2));
                return null;
            }
        }

        // English
        pattern = Pattern.compile("priority[\\s:]*([0-9]+)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                int priority = Integer.parseInt(matcher.group(1));
                int result = Math.min(10, Math.max(0, priority));
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
     */
    private LocalDateTime parseDate(String text) {
        if (text == null) return null;

        LocalDateTime now = LocalDateTime.now();
        text = text.toLowerCase();

        // "завтра"
        if (text.contains("завтра")) {
            LocalDateTime tomorrow = now.plusDays(1);
            Pattern timePattern = Pattern.compile("в\\s+(\\d{1,2}):(\\d{2})");
            Matcher timeMatcher = timePattern.matcher(text);
            if (timeMatcher.find()) {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = Integer.parseInt(timeMatcher.group(2));
                LocalDateTime result = tomorrow.withHour(hour).withMinute(minute).withSecond(0);
                log.debug("Найдена дата: завтра в {}:{}", hour, minute);
                return result;
            }
            log.debug("Найдена дата: завтра 09:00");
            return tomorrow.withHour(9).withMinute(0).withSecond(0);
        }

        // "через N дней"
        Pattern daysPattern = Pattern.compile("через\\s+(\\d+)\\s+(дн[яе]?)");
        Matcher daysMatcher = daysPattern.matcher(text);
        if (daysMatcher.find()) {
            int days = Integer.parseInt(daysMatcher.group(1));
            LocalDateTime future = now.plusDays(days);
            Pattern timePattern = Pattern.compile("в\\s+(\\d{1,2}):(\\d{2})");
            Matcher timeMatcher = timePattern.matcher(text);
            if (timeMatcher.find()) {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = Integer.parseInt(timeMatcher.group(2));
                log.debug("Найдена дата: через {} дней в {}:{}", days, hour, minute);
                return future.withHour(hour).withMinute(minute).withSecond(0);
            }
            log.debug("Найдена дата: через {} дней в 09:00", days);
            return future.withHour(9).withMinute(0).withSecond(0);
        }

        // "в N:MM"
        Pattern timePattern = Pattern.compile("в\\s+(\\d{1,2}):(\\d{2})");
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
        text = text.replaceAll("(приоритет|важность|priority)[\\s:]*[0-9]+", "").trim();

        // Убрать дату
        text = text.replaceAll("завтра", "").trim();
        text = text.replaceAll("через\\s+\\d+\\s+дн[яе]?", "").trim();
        text = text.replaceAll("в\\s+\\d{1,2}:\\d{2}", "").trim();
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
        if (priority != null && priority >= 7) {
            log.debug("Задача срочная (приоритет >= 7)");
            return true;
        }

        // Ключевые слова срочности
        boolean urgent = text.contains("срочно") ||
                text.contains("немедленно") ||
                text.contains("критично") ||
                text.contains("emergency") ||
                text.contains("urgent");

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
