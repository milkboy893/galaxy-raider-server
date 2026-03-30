package com.example.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

        // 重複チェック: 存在する場合は409を返し、Unity側で「別の名前を入力してください」とUI表示させる
        if (playerRepository.existsByName(name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("status", "error", "message", "Already registered"));
        }

        Player newPlayer = new Player();
        newPlayer.setName(name);
        playerRepository.save(newPlayer);

        return ResponseEntity.ok(Map.of("status", "success", "name", name));
    }

    @PostMapping("/score")
    public ResponseEntity<?> saveScore(@RequestBody Map<String, Object> body) {
        String playerName = (String) body.get("playerName");
        Integer scoreValue = (Integer) body.get("score");

        // UnityからはStringで名前が送られてくるため、Playerエンティティを探して紐づける
        Player player = playerRepository.findByName(playerName).orElse(null);
        if (player == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Player not found"));
        }

        Score score = new Score();
        score.setPlayer(player);
        score.setScore(scoreValue);
        scoreRepository.save(score);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @GetMapping("/ranking")
    public List<Map<String, Object>> getRanking() {
        return scoreRepository.findTop10ByOrderByScoreDesc().stream()
            .map(score -> Map.<String, Object>of(
                "playerName", score.getPlayer() != null ? score.getPlayer().getName() : "Guest",
                "score", score.getScore()
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/players/{name}/stats")
    public Map<String, Object> getPlayerStats(@PathVariable String name) {
        // TODO: データ量が増えるとfindAllは重くなるため、将来的にはSQL側で集計するようにService層へ切り出す
        List<Score> allScores = scoreRepository.findAll();

        int playCount = 0;
        int highScore = 0;
        List<Integer> history = new ArrayList<>();

        for (Score s : allScores) {
            if (s.getPlayer() != null && s.getPlayer().getName().equals(name)) {
                playCount++;
                if (s.getScore() > highScore) {
                    highScore = s.getScore();
                }
                history.add(s.getScore());
            }
        }

        // 履歴をスコアが高い順（降順）にソート
        history.sort(Collections.reverseOrder());

        // Unity側のUI表示（通信量）に合わせて、トップ5件のみに絞って返す
        if (history.size() > 5) {
            history = history.subList(0, 5);
        }

        return Map.of(
            "name", name,
            "playCount", playCount,
            "highScore", highScore,
            "history", history
        );
    }
}