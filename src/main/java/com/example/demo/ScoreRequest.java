package com.example.demo;

import java.io.Serializable;

public class ScoreRequest implements Serializable {
    private String playerName;
    private int score; // ★絶対に int（小文字）

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}