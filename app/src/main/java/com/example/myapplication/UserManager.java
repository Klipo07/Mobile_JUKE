package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {
    private static final String PREFS = "user_prefs";
    private static final String KEY_USER_ID = "current_user_id";
    private static final String KEY_USER_NAME = "current_user_name";

    public static void setCurrentUser(Context context, long userId, String name) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putLong(KEY_USER_ID, userId).putString(KEY_USER_NAME, name).apply();
    }

    public static long getCurrentUserId(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_USER_ID, -1);
    }

    public static String getCurrentUserName(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_USER_NAME, "");
    }
}


