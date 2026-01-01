package com.taskmanager.service;

import com.taskmanager.dao.AudioFileRepository;
import com.taskmanager.model.AudioFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AudioFileService {

    @Autowired
    private AudioFileRepository audioFileRepository;

    // Сохранить аудиофайл
    public AudioFile saveAudioFile(byte[] audioData, Integer duration, String fileName) {
        AudioFile audioFile = new AudioFile();
        audioFile.setAudioData(audioData);
        audioFile.setDurationSeconds(duration);
        audioFile.setFileName(fileName);
        audioFile.setCreatedAt(LocalDateTime.now());

        return audioFileRepository.save(audioFile);
    }

    // Получить аудиофайл по ID
    public Optional<AudioFile> getAudioFile(Long id) {
        return audioFileRepository.findById(id);
    }

    // Получить все аудиофайлы
    public List<AudioFile> getAllAudioFiles() {
        return audioFileRepository.findAll();
    }

    // Получить аудиофайлы в диапазоне дат
    public List<AudioFile> getAudioFilesByDateRange(LocalDateTime start, LocalDateTime end) {
        return audioFileRepository.findByCreatedAtBetween(start, end);
    }

    // Поиск по имени файла
    public List<AudioFile> searchByFileName(String name) {
        return audioFileRepository.findByFileNameContaining(name);
    }

    // Удалить аудиофайл
    public void deleteAudioFile(Long id) {
        audioFileRepository.deleteById(id);
    }
}
