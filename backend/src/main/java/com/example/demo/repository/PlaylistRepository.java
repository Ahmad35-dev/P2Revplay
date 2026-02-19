package com.example.demo.repository;

import com.example.demo.entity.Playlist;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUser(User user);
    List<Playlist> findByPrivacy(String privacy); // To fetch all PUBLIC playlists
}