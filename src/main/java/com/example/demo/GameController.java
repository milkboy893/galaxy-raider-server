package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // これを追加
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
                "playerName", score.getPlayerName(),
                "score", score.getScore(),
                "playDate", score.getPlayDate().toString()
            ))
            .collect(Collectors.toList());
    }

    // ▼▼▼ ここから下を追加 ▼▼▼

    // 1. Unityの RegisterPlayer が叩く窓口
    @PostMapping("/players/register")
    public Map<String, Object> registerPlayer(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        // とりあえず「登録成功」のサイン(200 OK)を返してUnityを先に進ませる
        return Map.of("status", "success", "name", name);
    }

    // 2. Unityの FetchPlayerStats が叩く窓口
    @GetMapping("/players/{name}/stats")
    public Map<String, Object> getPlayerStats(@PathVariable String name) {
        // とりあえずダミーの戦績データを返してエラーを防ぐ
        return Map.of(
            "name", name,
            "playCount", 1,
            "highScore", 0,
            "history", List.of()
        );
    }
}