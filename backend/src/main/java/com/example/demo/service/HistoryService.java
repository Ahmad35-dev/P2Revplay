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

    public void logSongPlay(String email, Long songId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        // Update play count
        song.setPlayCount(song.getPlayCount() + 1);
        songRepository.save(song);

        // Add to history
        History history = new History();
        history.setUser(user);
        history.setSong(song);
        history.setPlayedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    public List<HistoryDTO> getUserHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return historyRepository.findByUser_UserIdOrderByPlayedAtDesc(user.getUserId())
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private HistoryDTO mapToDTO(History history) {
        HistoryDTO dto = new HistoryDTO();
        dto.setHistoryId(history.getHistoryId());
        dto.setSongTitle(history.getSong().getTitle());
        dto.setCoverImageUrl(history.getSong().getCoverImageUrl());
        dto.setPlayedAt(history.getPlayedAt());

        if (history.getSong().getArtist() != null) {
            dto.setArtistName(history.getSong().getArtist().getUser().getName());
        }
        return dto;
    }
}