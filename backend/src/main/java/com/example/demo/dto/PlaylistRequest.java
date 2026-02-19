package com.example.demo.dto;

public class PlaylistRequest {
    private String name;
    private String description;
    private String privacy; // "PUBLIC" or "PRIVATE"

    // --- Getters and Setters ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPrivacy() { return privacy; }
    public void setPrivacy(String privacy) { this.privacy = privacy; }
}