package com.example.demo.repository;



import com.example.demo.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    // Find history for a specific user, ordered by most recent first
    List<History> findByUser_UserIdOrderByPlayedAtDesc(Long userId);
}