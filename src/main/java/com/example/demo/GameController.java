package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GameController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @PostMapping("/players/register")
    public ResponseEntity<?> registerPlayer(@RequestBody Map<String, String> request) {
        String name = request.get("name");

        if (playerRepository.existsByName(name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Name already taken");
        }

        Player player = new Player();
        player.setName(name);
        playerRepository.save(player);
        return ResponseEntity.ok("Registered successfully");
    }

    @PostMapping("/scores")
    public ResponseEntity<?> submitScore(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        int scoreValue = (int) request.get("score");

        Optional<Player> playerOpt = playerRepository.findByName(name);
        if (!playerOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Player not found");
        }

        Player player = playerOpt.get();
        player.setPlayCount(player.getPlayCount() + 1);
        playerRepository.save(player);

        Score score = new Score();
        score.setPlayer(player);
        score.setScore(scoreValue);
        scoreRepository.save(score);

        return ResponseEntity.ok("Score saved");
    }

    @GetMapping("/scores")
    public ResponseEntity<List<Map<String, Object>>> getRanking() {
        List<Score> topScores = scoreRepository.findTop10ByOrderByScoreDesc();

        List<Map<String, Object>> response = topScores.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", s.getPlayer().getName());
            map.put("score", s.getScore());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/players/{name}/stats")
    public ResponseEntity<?> getPlayerStats(@PathVariable String name) {
        Optional<Player> playerOpt = playerRepository.findByName(name);
        if (!playerOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Player not found");
        }

        Player player = playerOpt.get();
        List<Score> allScores = scoreRepository.findByPlayerIdOrderByScoreDesc(player.getId());

        int highScore = allScores.isEmpty() ? 0 : allScores.get(0).getScore();

        Map<String, Object> stats = new HashMap<>();
        stats.put("name", player.getName());
        stats.put("playCount", player.getPlayCount());
        stats.put("highScore", highScore);

        List<Integer> history = allScores.stream()
            .map(Score::getScore)
            .limit(5)
            .collect(Collectors.toList());
        stats.put("history", history);

        return ResponseEntity.ok(stats);
    }
}