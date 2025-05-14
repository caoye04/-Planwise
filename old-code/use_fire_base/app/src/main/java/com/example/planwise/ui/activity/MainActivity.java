package com.example.planwise.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.planwise.R;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;
import com.example.planwise.ui.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ScheduleViewModel scheduleViewModel;
    private UserViewModel userViewModel;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查Firebase认证状态
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d("MainActivity", "当前已登录: " + user.getEmail());
        } else {
            Log.d("MainActivity", "当前未登录");
        }

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 将登录结果转发给当前显示的Fragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null && navHostFragment.getChildFragmentManager().getFragments().size() > 0) {
            navHostFragment.getChildFragmentManager().getFragments().get(0)
                    .onActivityResult(requestCode, resultCode, data);
        }
    }
}