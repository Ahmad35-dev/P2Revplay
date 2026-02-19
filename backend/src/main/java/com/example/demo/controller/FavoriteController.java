package com.example.demo.controller;

import com.example.demo.service.CurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final CurationService curationService;

    public FavoriteController(CurationService curationService) {
        this.curationService = curationService;
    }

    @PostMapping("/{songId}")
    public ResponseEntity<String> toggleFavorite(Authentication authentication, @PathVariable Long songId) {
        String message = curationService.toggleFavorite(authentication.getName(), songId);
        return ResponseEntity.ok("{\"message\": \"" + message + "\"}");
    }
}