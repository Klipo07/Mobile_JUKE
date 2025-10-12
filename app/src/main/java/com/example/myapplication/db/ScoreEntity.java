package com.example.myapplication.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scores")
public class ScoreEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public int score;
    public int difficulty;
    public long createdAt;
}


