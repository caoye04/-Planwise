package com.example.planwise.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.planwise.R;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;
import com.example.planwise.ui.viewmodel.UserViewModel;

public class MainActivity extends AppCompatActivity {

    private ScheduleViewModel scheduleViewModel;
    private UserViewModel userViewModel;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup ViewModels
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Setup navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Connect BottomNavigationView with NavController
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Observe login state
        userViewModel.getIsLoggedIn().observe(this, isLoggedIn -> {
            // Optional: Handle login/logout state changes
        });
    }
}