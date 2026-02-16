package com.example.demo.entity;

import java.io.Serializable;
import java.util.Objects;

public class FollowedPlaylistId implements Serializable {
    private Long user;
    private Long playlist;

    public FollowedPlaylistId() {}

    // --- Getters and Setters ---
    public Long getUser() { return user; }
    public void setUser(Long user) { this.user = user; }

    public Long getPlaylist() { return playlist; }
    public void setPlaylist(Long playlist) { this.playlist = playlist; }

    // Required by Spring Boot for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowedPlaylistId that = (FollowedPlaylistId) o;
        return Objects.equals(user, that.user) && Objects.equals(playlist, that.playlist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, playlist);
    }
}