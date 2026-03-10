package com.example.demo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByPlayerIdOrderByScoreDesc(Long playerId);
    List<Score> findTop10ByOrderByScoreDesc();
}