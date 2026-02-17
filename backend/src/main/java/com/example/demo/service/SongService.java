package com.example.demo.service;

import com.example.demo.dto.SongDTO;
import com.example.demo.entity.Song;
import com.example.demo.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SongService {

    private final SongRepository songRepository;

    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    public List<SongDTO> getAllSongs() {
        List<Song> songs = songRepository.findAll();
        return songs.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<SongDTO> searchSongsByTitle(String title) {
        List<Song> songs = songRepository.findByTitleContainingIgnoreCase(title);
        return songs.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Helper method to convert the Database Entity into the Frontend DTO
    private SongDTO mapToDTO(Song song) {
        SongDTO dto = new SongDTO();
        dto.setSongId(song.getSongId());
        dto.setTitle(song.getTitle());
        dto.setGenre(song.getGenre());
        dto.setDuration(song.getDuration());
        dto.setPlayCount(song.getPlayCount());
        dto.setAudioFileUrl(song.getAudioFileUrl());
        dto.setCoverImageUrl(song.getCoverImageUrl());

        // Safely extract artist name if it exists
        if (song.getArtist() != null && song.getArtist().getUser() != null) {
            dto.setArtistName(song.getArtist().getUser().getName());
        } else {
            dto.setArtistName("Unknown Artist");
        }
        return dto;
    }
}