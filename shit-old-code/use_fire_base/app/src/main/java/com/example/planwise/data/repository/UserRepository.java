package com.example.planwise.data.repository;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.planwise.data.firebase.FirebaseAuthHelper;
import com.example.planwise.data.firebase.FirestoreHelper;
import com.example.planwise.data.model.User;
import com.google.firebase.auth.FirebaseUser;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private SharedPreferences prefs;
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();
    private FirebaseAuthHelper authHelper;
    private FirestoreHelper firestoreHelper;

    public UserRepository(Application application) {
        prefs = PreferenceManager.getDefaultSharedPreferences(application);
        authHelper = FirebaseAuthHelper.getInstance();
        firestoreHelper = FirestoreHelper.getInstance();

        // 设置Firebase认证回调
        authHelper.setAuthCallback(new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // 从Firebase用户创建应用用户
                User user = new User(
                        firebaseUser.getUid(),
                        firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                        firebaseUser.getEmail()
                );

                // 保存到SharedPreferences
                saveUserToPrefs(user);

                // 更新LiveData
                currentUser.setValue(user);
                isLoggedIn.setValue(true);

                // 保存用户数据到Firestore
                firestoreHelper.saveUserData(user.getUserId(), user.getUsername(), user.getEmail());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Authentication failed", e);
                isLoggedIn.setValue(false);
            }
        });

        // 检查是否已经登录
        FirebaseUser firebaseUser = authHelper.getCurrentUser();
        if (firebaseUser != null) {
            User user = new User(
                    firebaseUser.getUid(),
                    firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                    firebaseUser.getEmail()
            );
            currentUser.setValue(user);
            isLoggedIn.setValue(true);
        } else {
            isLoggedIn.setValue(false);
            loadUserFromPrefs(); // 尝试从本地加载
        }
    }

    private void saveUserToPrefs(User user) {
        prefs.edit()
                .putString("user_id", user.getUserId())
                .putString("username", user.getUsername())
                .putString("email", user.getEmail())
                .apply();
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

    public void login(String email, String username) {
        // 简单起见，使用邮箱作为密码
        String password = email;

        // 设置登录监听器
        authHelper.setAuthCallback(new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // 登录成功的处理保持不变
                Log.d(TAG, "登录成功：" + user.getEmail());
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "登录失败，尝试注册: " + e.getMessage());
                // 登录失败，尝试注册
                registerAndLogin(email, username);
            }
        });

        // 尝试登录
        authHelper.signInWithEmailAndPassword(email, password);
    }

    public void registerAndLogin(String email, String username) {
        // 简单起见，使用邮箱作为密码
        String password = email;

        // 注册新用户
        authHelper.createUserWithEmailAndPassword(email, password, username);
    }

    public void logout() {
        // 登出Firebase
        authHelper.signOut();

        // 清除SharedPreferences
        prefs.edit()
                .remove("user_id")
                .remove("username")
                .remove("email")
                .remove("photo_url")
                .apply();

        // 更新LiveData
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