package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 外部（Unityなど）からのアクセスを許可するおまじない
public class GameApiController {

    // 簡易的なランキング保存用リスト（※サーバー再起動でリセットされます）
    private static List<ScoreData> rankingList = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(GameApiController.class, args);
    }

    // ==========================================
    // ① Unityからスコアを受け取る窓口（POST）
    // URL: https://unity-tetris-server.onrender.com/api/score
    // ==========================================
    @PostMapping("/score")
    public String postScore(@RequestBody ScoreData data) {
        // 受け取ったデータをリストに追加
        rankingList.add(data);

        // スコアが高い順（降順）に並び替え
        rankingList.sort((a, b) -> b.score - a.score);

        System.out.println("新しいスコアを受信: " + data.name + " - " + data.score + "点");
        return "Success! Saved: " + data.name + " - " + data.score;
    }

    // ==========================================
    // ② Unityへランキングを返す窓口（GET）
    // URL: https://unity-tetris-server.onrender.com/api/ranking
    // ==========================================
    @GetMapping("/ranking")
    public Map<String, List<ScoreData>> getRanking() {
        // UnityのJsonUtilityが読み込みやすいように {"ranking": [データ, データ...]} の形にして返す
        return Collections.singletonMap("ranking", rankingList);
    }

    // ==========================================
    // データ構造（Unity側で送る "name", "score" と完全に一致させる）
    // ==========================================
    public static class ScoreData {
        public String name;
        public int score;
    }
}