package com.example.demo.repository;

import com.example.demo.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // Spring Boot automatically writes the SQL to search songs by title or genre!
    List<Song> findByTitleContainingIgnoreCase(String title);
    List<Song> findByGenreIgnoreCase(String genre);
}