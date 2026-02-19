package com.example.demo.entity;

import java.io.Serializable;
import java.util.Objects;

public class FavoriteId implements Serializable {

    // These MUST match the names of the @ManyToOne fields in Favorite.java
    private Long user;
    private Long song;

    public FavoriteId() {}

    public FavoriteId(Long user, Long song) {
        this.user = user;
        this.song = song;
    }

    public Long getUser() { return user; }
    public void setUser(Long user) { this.user = user; }

    public Long getSong() { return song; }
    public void setSong(Long song) { this.song = song; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteId that = (FavoriteId) o;
        return Objects.equals(user, that.user) && Objects.equals(song, that.song);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, song);
    }
}