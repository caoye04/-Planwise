package com.example.planwise.ui.viewmodel;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.planwise.data.model.User;
import com.example.planwise.data.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {
    private UserRepository repository;
    private LiveData<User> currentUser;
    private LiveData<Boolean> isLoggedIn;

    public UserViewModel(Application application) {
        super(application);
        repository = new UserRepository(application);
        currentUser = repository.getCurrentUser();
        isLoggedIn = repository.getIsLoggedIn();
    }

    public void login(String userId, String username, String email) {
        repository.login(userId, username, email);
    }

    public void logout() {
        repository.logout();
    }

    public void updateSyncPreference(boolean enabled) {
        repository.updateSyncPreference(enabled);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }
}