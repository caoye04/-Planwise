package com.example.planwise.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.planwise.data.model.User;
import com.example.planwise.data.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {
    private UserRepository repository;
    private LiveData<User> currentUser;
    private LiveData<Boolean> isLoggedIn;
    private MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public UserViewModel(Application application) {
        super(application);
        repository = new UserRepository(application);
        currentUser = repository.getCurrentUser();
        isLoggedIn = repository.getIsLoggedIn();
    }

    public void login(String email, String username) {
        repository.login(email, username);
    }

    public void registerAndLogin(String email, String username) {
        repository.registerAndLogin(email, username);
    }

    public void logout() {
        repository.logout();
    }

    public void updateSyncPreference(boolean enabled) {
        repository.updateSyncPreference(enabled);
    }

    public void setSyncing(boolean syncing) {
        isSyncing.setValue(syncing);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }
}