package com.example.myapplication.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UserEntity.class, ScoreEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppDao dao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "bugs_db").build();
                }
            }
        }
        return INSTANCE;
    }
}


