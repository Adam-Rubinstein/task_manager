package com.taskmanager.service;

import com.taskmanager.dao.AudioFileRepository;
import com.taskmanager.model.AudioFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AudioFileService {

    private static final Logger log = LoggerFactory.getLogger(AudioFileService.class);

    @Autowired
    private AudioFileRepository audioFileRepository;

    // Сохранить аудиофайл
    public AudioFile saveAudioFile(byte[] audioData, Integer duration, String fileName) {
        log.debug("Сохранение аудиофайла: fileName={}, duration={}s", fileName, duration);

        AudioFile audioFile = new AudioFile();
        audioFile.setAudioData(audioData);
        audioFile.setDurationSeconds(duration);
        audioFile.setFileName(fileName);
        audioFile.setCreatedAt(LocalDateTime.now());

        AudioFile saved = audioFileRepository.save(audioFile);
        log.info("Аудиофайл сохранён: id={}, fileName={}", saved.getId(), fileName);

        return saved;
    }

    // Получить аудиофайл по ID
    public Optional<AudioFile> getAudioFile(Long id) {
        log.debug("Загрузка аудиофайла: id={}", id);
        Optional<AudioFile> audioFile = audioFileRepository.findById(id);

        if (audioFile.isPresent()) {
            log.debug("Аудиофайл найден: id={}", id);
        } else {
            log.warn("Аудиофайл не найден: id={}", id);
        }

        return audioFile;
    }

    // Получить все аудиофайлы
    public List<AudioFile> getAllAudioFiles() {
        log.debug("Загрузка всех аудиофайлов");
        List<AudioFile> files = audioFileRepository.findAll();
        log.debug("Найдено аудиофайлов: {}", files.size());
        return files;
    }

    // Получить аудиофайлы в диапазоне дат
    public List<AudioFile> getAudioFilesByDateRange(LocalDateTime start, LocalDateTime end) {
        log.debug("Загрузка аудиофайлов в диапазоне: start={}, end={}", start, end);
        List<AudioFile> files = audioFileRepository.findByCreatedAtBetween(start, end);
        log.debug("Найдено аудиофайлов: {}", files.size());
        return files;
    }

    // Поиск по имени файла
    public List<AudioFile> searchByFileName(String name) {
        log.debug("Поиск аудиофайлов по имени: name={}", name);
        List<AudioFile> files = audioFileRepository.findByFileNameContaining(name);
        log.debug("Найдено аудиофайлов: {}", files.size());
        return files;
    }

    // Удалить аудиофайл
    public void deleteAudioFile(Long id) {
        log.debug("Удаление аудиофайла: id={}", id);
        audioFileRepository.deleteById(id);
        log.info("Аудиофайл удалён: id={}", id);
    }
}
