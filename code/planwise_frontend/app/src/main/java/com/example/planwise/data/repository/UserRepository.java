package com.example.planwise.data.repository;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.planwise.data.model.User;

public class UserRepository {
    private SharedPreferences prefs;
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();

    public UserRepository(Application application) {
        prefs = PreferenceManager.getDefaultSharedPreferences(application);
        isLoggedIn.setValue(isUserLoggedIn());
        if (isUserLoggedIn()) {
            loadUserFromPrefs();
        }
    }

    private boolean isUserLoggedIn() {
        return prefs.contains("user_id");
    }

    private void loadUserFromPrefs() {
        String userId = prefs.getString("user_id", null);
        String username = prefs.getString("username", null);
        String email = prefs.getString("email", null);
        String photoUrl = prefs.getString("photo_url", null);
        boolean syncEnabled = prefs.getBoolean("sync_enabled", false);

        if (userId != null && username != null) {
            User user = new User(userId, username, email);
            user.setPhotoUrl(photoUrl);
            user.setSyncEnabled(syncEnabled);
            currentUser.setValue(user);
        }
    }

    public void login(String userId, String username, String email) {
        User user = new User(userId, username, email);

        // Save to preferences
        prefs.edit()
                .putString("user_id", userId)
                .putString("username", username)
                .putString("email", email)
                .apply();

        currentUser.setValue(user);
        isLoggedIn.setValue(true);
    }

    public void logout() {
        prefs.edit()
                .remove("user_id")
                .remove("username")
                .remove("email")
                .remove("photo_url")
                .apply();

        currentUser.setValue(null);
        isLoggedIn.setValue(false);
    }

    public void updateSyncPreference(boolean enabled) {
        User user = currentUser.getValue();
        if (user != null) {
            user.setSyncEnabled(enabled);
            currentUser.setValue(user);

            prefs.edit()
                    .putBoolean("sync_enabled", enabled)
                    .apply();
        }
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }
}