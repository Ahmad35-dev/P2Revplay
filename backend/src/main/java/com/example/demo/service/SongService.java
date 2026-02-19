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
        return songRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<SongDTO> searchSongsByTitle(String title) {
        return songRepository.findByTitleContainingIgnoreCase(title)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // NEW: Pass the filters to the repository
    public List<SongDTO> filterSongs(String genre, String artistName, String albumName, Integer releaseYear) {
        return songRepository.filterSongs(genre, artistName, albumName, releaseYear)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Helper method to convert Entity to DTO safely
    private SongDTO mapToDTO(Song song) {
        SongDTO dto = new SongDTO();
        dto.setSongId(song.getSongId());
        dto.setTitle(song.getTitle());
        dto.setGenre(song.getGenre());
        dto.setDuration(song.getDuration());
        dto.setPlayCount(song.getPlayCount());
        dto.setAudioFileUrl(song.getAudioFileUrl());
        dto.setCoverImageUrl(song.getCoverImageUrl());

        if (song.getArtist() != null && song.getArtist().getUser() != null) {
            dto.setArtistName(song.getArtist().getUser().getName());
        }
        if (song.getAlbum() != null) {
            dto.setAlbumName(song.getAlbum().getTitle());
        }

        return dto;
    }
}