package com.example.demo.dto;

public class AuthResponse {
    private String token;
    private String name;
    private String role;
    private Long userId;

    public AuthResponse(String token, String name, String role, Long userId) {
        this.token = token;
        this.name = name;
        this.role = role;
        this.userId = userId;
    }

    // --- Getters ---
    public String getToken() { return token; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public Long getUserId() { return userId; }
}