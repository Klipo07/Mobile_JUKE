package com.example.myapplication.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(UserEntity user);

    @Query("SELECT * FROM users ORDER BY id DESC")
    List<UserEntity> getUsers();

    @Query("SELECT * FROM users WHERE name = :name LIMIT 1")
    UserEntity getUserByName(String name);

    @Query("SELECT COUNT(*) FROM users")
    int getUsersCount();

    @Query("DELETE FROM scores WHERE userId = :userId")
    void deleteScoresByUser(long userId);

    @Query("DELETE FROM users WHERE id = :userId")
    void deleteUserById(long userId);

    @Insert
    long insertScore(ScoreEntity score);

    @Query("SELECT scores.id, users.name, scores.score, scores.difficulty, scores.createdAt FROM scores JOIN users ON users.id = scores.userId ORDER BY scores.score DESC LIMIT 100")
    List<RecordItem> getTopRecords();
}


