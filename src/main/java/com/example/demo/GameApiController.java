package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Unityからのアクセスをブロックされないためのおまじない
public class GameApiController {

    // 簡易的なランキング保存用リスト（サーバー再起動で消えます）
    private static List<ScoreData> rankingList = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(GameApiController.class, args);
    }

    // ① Unityからスコアを受け取る（POST）
    @PostMapping("/score")
    public String postScore(@RequestBody ScoreData data) {
        rankingList.add(data);
        // スコアが高い順（降順）に並び替え
        rankingList.sort((a, b) -> b.score - a.score);
        return "Success! Saved: " + data.name + " - " + data.score;
    }

    // ② Unityにランキングを返す（GET）
    @GetMapping("/ranking")
    public Map<String, List<ScoreData>> getRanking() {
        // UnityのJsonUtilityが読み込みやすいように {"ranking": [データ]} の形式にして返す
        return Collections.singletonMap("ranking", rankingList);
    }

    // データ構造
    public static class ScoreData {
        public String name;
        public int score;
    }
}