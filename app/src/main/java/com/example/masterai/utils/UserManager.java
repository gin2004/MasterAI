package com.example.masterai.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.masterai.model.User;
import com.google.gson.Gson;

public class UserManager {
    private static final String PREF_NAME = "MasterAI_Prefs";
    private static final String KEY_USER = "current_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static UserManager instance;
    private SharedPreferences sharedPreferences;
    private User currentUser;
    private Gson gson;

    private UserManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadUserFromPrefs();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setUser(User user) {
        this.currentUser = user;
        String userJson = gson.toJson(user);
        sharedPreferences.edit()
                .putString(KEY_USER, userJson)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public User getUser() {
        if (currentUser == null) {
            loadUserFromPrefs();
        }
        return currentUser;
    }

    private void loadUserFromPrefs() {
        String userJson = sharedPreferences.getString(KEY_USER, null);
        if (userJson != null) {
            currentUser = gson.fromJson(userJson, User.class);
        }
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        currentUser = null;
        sharedPreferences.edit()
                .remove(KEY_USER)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }
}