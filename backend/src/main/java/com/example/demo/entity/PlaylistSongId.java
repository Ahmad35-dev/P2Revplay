package com.example.demo.entity;

import java.io.Serializable;
import java.util.Objects;

public class PlaylistSongId implements Serializable {
    private Long playlist;
    private Long song;

    public PlaylistSongId() {}

    public Long getPlaylist() { return playlist; }
    public void setPlaylist(Long playlist) { this.playlist = playlist; }

    public Long getSong() { return song; }
    public void setSong(Long song) { this.song = song; }

    // Spring Boot strictly requires equals() and hashCode() for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaylistSongId that = (PlaylistSongId) o;
        return Objects.equals(playlist, that.playlist) && Objects.equals(song, that.song);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlist, song);
    }
}