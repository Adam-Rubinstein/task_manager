package com.taskmanager.dao;

import com.taskmanager.model.AudioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AudioFileRepository extends JpaRepository<AudioFile, Long> {
    List<AudioFile> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<AudioFile> findByFileNameContaining(String name);
}
