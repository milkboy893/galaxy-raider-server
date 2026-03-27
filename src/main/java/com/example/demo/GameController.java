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

    // ① スコアを保存する
    @PostMapping("/score")
    public Score saveScore(@RequestBody Score score) {
        return scoreRepository.save(score);
    }

    // ② ランキングを取得する
    @GetMapping("/ranking")
    public List<Map<String, Object>> getRanking() {
        return scoreRepository.findTop10ByOrderByScoreDesc().stream()
            .map(score -> Map.<String, Object>of(
                "playerName", score.getPlayerName(),
                "score", score.getScore(),
                "playDate", score.getPlayDate().toString()
            ))
            .collect(Collectors.toList());
    }

    // ③ 【新規追加】Unityからのプレイヤー登録を受け止める（404回避）
    @PostMapping("/players/register")
    public Map<String, Object> registerPlayer(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        // エラーを出さず「成功」としてUnityを先に進ませる
        return Map.of("status", "success", "name", name);
    }

    // ④ 【新規追加】Unityからの戦績リクエストを受け止める（404回避）
    @GetMapping("/players/{name}/stats")
    public Map<String, Object> getPlayerStats(@PathVariable String name) {
        // ゲームが止まらないように、仮の戦績データを返す
        return Map.of(
            "name", name,
            "playCount", 1,
            "highScore", 0,
            "history", List.of()
        );
    }
}