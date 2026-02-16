package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "playlist_songs")
@IdClass(PlaylistSongId.class)
public class PlaylistSong {

    @Id
    @ManyToOne
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @Id
    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(name = "song_order", nullable = false)
    private Integer songOrder;

    public PlaylistSong() {}

    // --- Getters and Setters ---
    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }

    public Song getSong() { return song; }
    public void setSong(Song song) { this.song = song; }

    public Integer getSongOrder() { return songOrder; }
    public void setSongOrder(Integer songOrder) { this.songOrder = songOrder; }
}