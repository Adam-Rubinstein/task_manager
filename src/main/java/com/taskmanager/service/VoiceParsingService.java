package com.taskmanager.service;

import com.taskmanager.dto.VoiceTaskParsed;
import com.joestelmach.natty.Parser;
import com.joestelmach.natty.DateGroup;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VoiceParsingService - парсинг голосовых сообщений на русском языке (ФАЗА 2)
 * 
 * Функции:
 * 1. Парсинг дат (Natty): "завтра в 15:00", "через 3 дня", "в понедельник"
 * 2. Парсинг приоритета (Regex): "приоритет 8", "важность 7"
 * 3. Очистка текста для названия задачи
 * 4. Определение срочности
 * 
 * Пример:
 * Input:  "Купить молоко завтра в 15:00, приоритет 8"
 * Output: {
 *   title: "Купить молоко",
 *   dueDate: 2026-01-02T15:00:00,
 *   priority: 8,
 *   isUrgent: false
 * }
 */
@Service
public class VoiceParsingService {

    private static final Parser nattyParser = new Parser();

    // Regex для парсинга приоритета
    private static final Pattern PRIORITY_PATTERN_RU = Pattern.compile(
            "(?:приоритет|важность)\\s+(\\d+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern PRIORITY_PATTERN_EN = Pattern.compile(
            "(?:priority|importance)\\s+(\\d+)",
            Pattern.CASE_INSENSITIVE
    );

    // Regex для определения срочности
    private static final Pattern URGENT_PATTERN = Pattern.compile(
            "(срочно|urgent|спешно|asap|немедленно|быстро|экстренно)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    /**
     * Основной метод парсинга голосового текста
     * @param text Исходный текст (например, "Купить молоко завтра в 15:00, приоритет 8")
     * @return VoiceTaskParsed с распарсенными данными
     */
    public VoiceTaskParsed parseVoiceText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        VoiceTaskParsed result = new VoiceTaskParsed();

        // 1. Парсинг приоритета
        Integer priority = parsePriority(text);
        result.setPriority(priority != null ? priority : 5); // По умолчанию 5

        // 2. Парсинг срочности
        Boolean isUrgent = isUrgent(text);
        result.setIsUrgent(isUrgent);

        // 3. Парсинг даты
        LocalDateTime dueDate = parseDateWithNatty(text);
        result.setDueDate(dueDate);

        // 4. Очистка текста для названия задачи
        String cleanedTitle = cleanText(text);
        result.setTitle(cleanedTitle);

        // 5. Описание (пока пусто, можно расширить)
        result.setDescription("");

        return result;
    }

    /**
     * Парсинг даты с помощью Natty
     * 
     * Поддерживаемые форматы на русском:
     * - "завтра в 15:00"
     * - "через 3 дня"
     * - "в понедельник"
     * - "в 15:00"
     * - "завтра"
     * - "сегодня"
     * 
     * @param text Текст для парсинга
     * @return LocalDateTime или null если не найдено
     */
    private LocalDateTime parseDateWithNatty(String text) {
        try {
            // Переводим русские даты на английские для Natty
            String translatedText = translateRussianDates(text);

            // Парсим с помощью Natty
            List<DateGroup> groups = nattyParser.parse(translatedText);

            if (groups != null && !groups.isEmpty()) {
                DateGroup dateGroup = groups.get(0);
                List<Date> dates = dateGroup.getDates();

                if (dates != null && !dates.isEmpty()) {
                    Date date = dates.get(0);
                    return date.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при парсинге даты: " + e.getMessage());
        }

        return null;
    }

    /**
     * Перевод русских дат на английские для Natty
     * 
     * Примеры:
     * "завтра" → "tomorrow"
     * "сегодня" → "today"
     * "через 3 дня" → "in 3 days"
     * "в понедельник" → "on Monday"
     * 
     * @param text Исходный текст
     * @return Текст с переведёнными датами
     */
    private String translateRussianDates(String text) {
        // Замены русских дат на английские
        text = text.replaceAll("\\bзавтра\\b", "tomorrow");
        text = text.replaceAll("\\bсегодня\\b", "today");
        text = text.replaceAll("\\bпосле завтрашнего дня\\b", "day after tomorrow");

        // Дни недели
        text = text.replaceAll("\\bпонедельник\\b", "Monday");
        text = text.replaceAll("\\bвторник\\b", "Tuesday");
        text = text.replaceAll("\\bсреда\\b", "Wednesday");
        text = text.replaceAll("\\bчетверг\\b", "Thursday");
        text = text.replaceAll("\\bпятница\\b", "Friday");
        text = text.replaceAll("\\bсуббота\\b", "Saturday");
        text = text.replaceAll("\\bвоскресенье\\b", "Sunday");

        // "через N дней/часов"
        text = text.replaceAll("\\bчерез\\s+(\\d+)\\s+дн[её]й\\b", "in $1 days");
        text = text.replaceAll("\\bчерез\\s+(\\d+)\\s+час[ов]*\\b", "in $1 hours");
        text = text.replaceAll("\\bчерез\\s+(\\d+)\\s+минут[ы]*\\b", "in $1 minutes");

        // "в N часов/часа"
        text = text.replaceAll("\\bв\\s+(\\d+)\\s*:\\s*(\\d+)\\b", "at $1:$2");

        return text;
    }

    /**
     * Парсинг приоритета из текста
     * 
     * Примеры:
     * "приоритет 8" → 8
     * "priority 5" → 5
     * "важность 7" → 7
     * 
     * @param text Текст для парсинга
     * @return Приоритет (0-10) или null если не найдено
     */
    private Integer parsePriority(String text) {
        if (text == null) {
            return null;
        }

        // Русский вариант
        Matcher matcherRu = PRIORITY_PATTERN_RU.matcher(text);
        if (matcherRu.find()) {
            try {
                int priority = Integer.parseInt(matcherRu.group(1));
                return Math.min(10, Math.max(0, priority)); // Ограничиваем 0-10
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Английский вариант
        Matcher matcherEn = PRIORITY_PATTERN_EN.matcher(text);
        if (matcherEn.find()) {
            try {
                int priority = Integer.parseInt(matcherEn.group(1));
                return Math.min(10, Math.max(0, priority)); // Ограничиваем 0-10
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Проверка срочности задачи
     * 
     * @param text Текст для проверки
     * @return true если содержит маркеры срочности
     */
    private Boolean isUrgent(String text) {
        if (text == null) {
            return false;
        }

        Matcher matcher = URGENT_PATTERN.matcher(text);
        return matcher.find();
    }

    /**
     * Очистка текста для получения названия задачи
     * 
     * Удаляет:
     * - Даты и время
     * - Приоритет
     * - Лишние пробелы
     * 
     * @param text Исходный текст
     * @return Очищенный текст (название задачи)
     */
    private String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String cleaned = text;

        // Удаляем приоритет и его вариации
        cleaned = cleaned.replaceAll(
                "(?:,?\\s*(?:приоритет|важность|priority|importance)\\s+\\d+)",
                ""
        );

        // Удаляем сроки и даты
        cleaned = cleaned.replaceAll(
                "(?:,?\\s*(?:завтра|сегодня|через\\s+\\d+\\s+(?:дн[её]й|часов|минут)))",
                ""
        );

        // Удаляем время (HH:MM)
        cleaned = cleaned.replaceAll("\\s+\\d{1,2}:\\d{2}\\b", "");

        // Удаляем срочность маркеры
        cleaned = cleaned.replaceAll(
                "(?:,?\\s*(?:срочно|urgent|спешно|asap|немедленно|быстро|экстренно|!))",
                ""
        );

        // Удаляем множественные пробелы
        cleaned = cleaned.replaceAll("\\s+", " ");

        // Удаляем пробелы и запятые в начале и конце
        cleaned = cleaned.trim();
        cleaned = cleaned.replaceAll("^[,\\s]+", "");
        cleaned = cleaned.replaceAll("[,\\s]+$", "");

        return cleaned;
    }

    /**
     * Валидация распарсенных данных
     * 
     * @param parsed VoiceTaskParsed для валидации
     * @return true если данные корректные
     */
    public Boolean isValidParsed(VoiceTaskParsed parsed) {
        if (parsed == null) {
            return false;
        }

        // Название не может быть пустым
        if (parsed.getTitle() == null || parsed.getTitle().isEmpty()) {
            return false;
        }

        // Приоритет должен быть в диапазоне 0-10
        if (parsed.getPriority() == null || parsed.getPriority() < 0 || parsed.getPriority() > 10) {
            return false;
        }

        return true;
    }
}