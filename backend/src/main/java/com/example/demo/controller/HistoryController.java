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

    // POST: http://localhost:8080/api/history/log?songId=1
    @PostMapping("/log")
    public ResponseEntity<String> logPlay(@RequestParam Long songId, Authentication authentication) {
        String email = authentication.getName();
        historyService.logSongPlay(email, songId);
        return ResponseEntity.ok("Song play logged successfully.");
    }

    // GET: http://localhost:8080/api/history
    @GetMapping
    public ResponseEntity<List<HistoryDTO>> getHistory(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(historyService.getUserHistory(email));
    }
}