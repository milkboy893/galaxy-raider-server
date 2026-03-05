package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/scores")
@CrossOrigin(origins = "*")
public class ScoreController {

    @Autowired
    private ScoreRepository repository;

    @GetMapping
    public List<Score> getTopScores() {
        return repository.findTop5ByOrderByScoreDesc();
    }

    @PostMapping
    public Score addScore(@RequestBody Score score) {
        return repository.save(score);
    }
}