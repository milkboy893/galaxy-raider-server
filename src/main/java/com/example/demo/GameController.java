package com.example.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/score")
    public Score saveScore(@RequestBody Score score) {
        return scoreRepository.save(score);
    }

    @GetMapping("/ranking")
    public List<Map<String, Object>> getRanking() {
        return scoreRepository.findTop10ByOrderByScoreDesc().stream()
            .map(score -> Map.<String, Object>of(
                "playerName", score.getPlayerName() != null ? score.getPlayerName() : "Guest",
                "score", score.getScore()
            ))
            .collect(Collectors.toList());
    }

    @PostMapping("/players/register")
    public Map<String, Object> registerPlayer(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        return Map.of("status", "success", "name", name);
    }

    @GetMapping("/players/{name}/stats")
    public Map<String, Object> getPlayerStats(@PathVariable String name) {
        List<Score> allScores = scoreRepository.findAll();

        int playCount = 0;
        int highScore = 0;
        List<Integer> history = new ArrayList<>();

        for (Score s : allScores) {
            if (s.getPlayerName() != null && s.getPlayerName().equals(name)) {
                playCount++;
                if (s.getScore() > highScore) {
                    highScore = s.getScore();
                }
                // 該当プレイヤーのスコアを履歴リストに追加
                history.add(s.getScore());
            }
        }

        // 履歴を高い順（降順）に並べ替える
        history.sort(Collections.reverseOrder());

        // Unity側のUIに合わせて、トップ5件だけを送る（多すぎると通信の無駄になるため）
        if (history.size() > 5) {
            history = history.subList(0, 5);
        }

        return Map.of(
            "name", name,
            "playCount", playCount,
            "highScore", highScore,
            "history", history // ここで本当のスコア履歴を渡す！
        );
    }
}