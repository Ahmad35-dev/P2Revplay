package com.example.demo.service;

import com.example.demo.dto.HistoryDTO;
import com.example.demo.entity.History;
import com.example.demo.entity.Song;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.HistoryRepository;
import com.example.demo.repository.SongRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    public HistoryService(HistoryRepository historyRepository, UserRepository userRepository, SongRepository songRepository) {
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
    }

    @Transactional
    public void logSongPlay(String email, Long songId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        // FIXED: Reverted back to your original code!
        // Since playCount is a primitive 'int', it defaults to 0 and can never be null.
        song.setPlayCount(song.getPlayCount() + 1);
        songRepository.save(song);

        // Add to history
        History history = new History();
        history.setUser(user);
        history.setSong(song);
        history.setPlayedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    // UPDATED: Get complete listening history
    public List<HistoryDTO> getCompleteHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return historyRepository.findByUser_UserIdOrderByPlayedAtDesc(user.getUserId())
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // NEW FEATURE: Get recent 50 songs
    public List<HistoryDTO> getRecentHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return historyRepository.findTop50ByUser_UserIdOrderByPlayedAtDesc(user.getUserId())
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // NEW FEATURE: Clear history
    @Transactional
    public void clearHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        historyRepository.deleteByUser(user);
    }

    // Keeps your existing DTO mapping logic
    private HistoryDTO mapToDTO(History history) {
        HistoryDTO dto = new HistoryDTO();
        dto.setHistoryId(history.getHistoryId());
        dto.setSongTitle(history.getSong().getTitle());
        dto.setCoverImageUrl(history.getSong().getCoverImageUrl());
        dto.setPlayedAt(history.getPlayedAt());

        if (history.getSong().getArtist() != null && history.getSong().getArtist().getUser() != null) {
            dto.setArtistName(history.getSong().getArtist().getUser().getName());
        }
        return dto;
    }
    // NEW FEATURE: Calculate total listening time
    public String getTotalListeningTime(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long totalSeconds = historyRepository.calculateTotalListeningTimeInSeconds(user);

        // Logic to make it readable: e.g., "120 minutes" or "2 hours"
        long minutes = totalSeconds / 60;
        if (minutes < 60) {
            return minutes + " minutes";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + " hours, " + remainingMinutes + " minutes";
        }
    }
}