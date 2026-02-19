package com.example.demo.repository;

import com.example.demo.entity.Playlist;
import com.example.demo.entity.PlaylistSong;
import com.example.demo.entity.PlaylistSongId;
import com.example.demo.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, PlaylistSongId> {
    Optional<PlaylistSong> findByPlaylistAndSong(Playlist playlist, Song song);
}