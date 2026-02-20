package com.example.demo.controller;

import com.example.demo.dto.HistoryDTO;
import com.example.demo.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    // EXISTING: POST: http://localhost:8080/api/history/log?songId=1
    @PostMapping("/log")
    public ResponseEntity<String> logPlay(@RequestParam Long songId, Authentication authentication) {
        String email = authentication.getName();
        historyService.logSongPlay(email, songId);
        // Returning as a JSON string so the Angular frontend can parse it easily!
        return ResponseEntity.ok("{\"message\": \"Song play logged successfully.\"}");
    }

    // NEW FEATURE: GET: http://localhost:8080/api/history/recent
    // Fetches only the last 50 played songs
    @GetMapping("/recent")
    public ResponseEntity<List<HistoryDTO>> getRecentHistory(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(historyService.getRecentHistory(email));
    }

    // UPDATED: GET: http://localhost:8080/api/history/all
    // Fetches the entire listening history with timestamps
    @GetMapping("/all")
    public ResponseEntity<List<HistoryDTO>> getCompleteHistory(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(historyService.getCompleteHistory(email));
    }

    // NEW FEATURE: DELETE: http://localhost:8080/api/history/clear
    // Wipes the listening history for privacy
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearHistory(Authentication authentication) {
        String email = authentication.getName();
        historyService.clearHistory(email);
        return ResponseEntity.ok("{\"message\": \"Listening history cleared successfully.\"}");
    }
    // NEW FEATURE: GET: http://localhost:8080/api/history/stats/time
    @GetMapping("/stats/time")
    public ResponseEntity<String> getTotalTime(Authentication authentication) {
        String email = authentication.getName();
        String timeStats = historyService.getTotalListeningTime(email);
        return ResponseEntity.ok("{\"totalListeningTime\": \"" + timeStats + "\"}");
    }
}