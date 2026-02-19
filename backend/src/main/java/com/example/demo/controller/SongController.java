package com.example.demo.controller;

import com.example.demo.dto.SongDTO;
import com.example.demo.service.SongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @GetMapping
    public ResponseEntity<List<SongDTO>> getAllSongs() {
        return ResponseEntity.ok(songService.getAllSongs());
    }

    @GetMapping("/search")
    public ResponseEntity<List<SongDTO>> searchSongs(@RequestParam String title) {
        return ResponseEntity.ok(songService.searchSongsByTitle(title));
    }

    // NEW: Advanced Filtering Endpoint
    // Example: GET http://localhost:8080/api/songs/filter?genre=Pop&year=2026
    @GetMapping("/filter")
    public ResponseEntity<List<SongDTO>> filterSongs(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false, name = "artist") String artistName,
            @RequestParam(required = false, name = "album") String albumName,
            @RequestParam(required = false, name = "year") Integer releaseYear) {

        return ResponseEntity.ok(songService.filterSongs(genre, artistName, albumName, releaseYear));
    }
}