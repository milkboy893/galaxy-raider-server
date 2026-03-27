package com.example.demo;

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

    // ★修正1: playDateによるエラー（クラッシュ）を防ぐため、確実に存在する名前とスコアだけを返す
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

    // ★修正2: 仮データではなく、DBから本当のプレイ回数とハイスコアを計算して返す
    @GetMapping("/players/{name}/stats")
    public Map<String, Object> getPlayerStats(@PathVariable String name) {
        List<Score> allScores = scoreRepository.findAll();

        int playCount = 0;
        int highScore = 0;

        for (Score s : allScores) {
            // nullチェックをしつつ、該当プレイヤーのスコアを探す
            if (s.getPlayerName() != null && s.getPlayerName().equals(name)) {
                playCount++;
                if (s.getScore() > highScore) {
                    highScore = s.getScore();
                }
            }
        }

        return Map.of(
            "name", name,
            "playCount", playCount,
            "highScore", highScore,
            "history", List.of() // 履歴リストは空のままでUIエラーを防ぐ
        );
    }
}