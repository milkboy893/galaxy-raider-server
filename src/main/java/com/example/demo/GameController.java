package com.example.demo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GameController {

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @PostMapping("/players/register")
    public ResponseEntity<?> registerPlayer(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || playerRepository.existsByName(name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error");
        }
        Player newPlayer = new Player();
        newPlayer.setName(name);
        playerRepository.save(newPlayer);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/score")
    public ResponseEntity<?> saveScore(@RequestBody ScoreRequest request) {
        // 1. 変数に取り出す（この時点で int なので絶対に null ではありません）
        String pName = request.getPlayerName();
        int scoreValue = request.getScore();

        if (pName == null) {
            return ResponseEntity.badRequest().body("Name is null");
        }

        // 2. Optional の map を使わず、isPresent で確実に分岐させる
        // これにより IDE は「player が確実に存在する状態」と「値が安全な状態」を確信します
        Optional<Player> playerOpt = playerRepository.findByName(pName);

        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            Score score = new Score();
            score.setPlayer(player);
            score.setScore(scoreValue); // ★ここでの Unboxing 警告は物理的に出なくなります
            scoreRepository.save(score);
            return ResponseEntity.ok("Success");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Player not found");
    }

    @GetMapping("/ranking")
    public List<Map<String, Object>> getRanking() {
        return scoreRepository.findTop10ByOrderByScoreDesc().stream()
            .map(s -> {
                Map<String, Object> map = new HashMap<>();
                map.put("playerName", s.getPlayer() != null ? s.getPlayer().getName() : "Guest");
                map.put("score", s.getScore());
                return map;
            })
            .collect(Collectors.toList());
    }

    @GetMapping("/players/{name}/stats")
    public Map<String, Object> getPlayerStats(@PathVariable String name) {
        List<Score> all = scoreRepository.findAll();
        List<Integer> userScores = all.stream()
            .filter(s -> s.getPlayer() != null && name.equals(s.getPlayer().getName()))
            .map(Score::getScore)
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        // ここも波線が出ないように安全に処理
        int pCount = userScores.size();
        int hScore = 0;
        if (!userScores.isEmpty() && userScores.get(0) != null) {
            hScore = userScores.get(0);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("name", name);
        stats.put("playCount", pCount);
        stats.put("highScore", hScore);
        stats.put("history", userScores.stream().limit(5).collect(Collectors.toList()));
        return stats;
    }
}