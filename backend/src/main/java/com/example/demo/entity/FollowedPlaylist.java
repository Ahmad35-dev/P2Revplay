package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "followed_playlists")
@IdClass(FollowedPlaylistId.class)
public class FollowedPlaylist {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    public FollowedPlaylist() {}

    // --- Getters and Setters ---
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }
}