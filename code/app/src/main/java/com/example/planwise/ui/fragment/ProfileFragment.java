package com.example.planwise.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.planwise.R;
import com.example.planwise.data.model.User;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;
import com.example.planwise.ui.viewmodel.UserViewModel;

public class ProfileFragment extends Fragment {

    private UserViewModel userViewModel;
    private ScheduleViewModel scheduleViewModel;

    private TextView textViewUsername;
    private TextView textViewEmail;
    private Switch switchSync;
    private Button buttonLogin;
    private Button buttonLogout;
    private Button buttonSyncNow;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup ViewModels
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        scheduleViewModel = new ViewModelProvider(requireActivity()).get(ScheduleViewModel.class);

        // Initialize views
        textViewUsername = view.findViewById(R.id.text_view_username);
        textViewEmail = view.findViewById(R.id.text_view_email);
        switchSync = view.findViewById(R.id.switch_sync);
        buttonLogin = view.findViewById(R.id.button_login);
        buttonLogout = view.findViewById(R.id.button_logout);
        buttonSyncNow = view.findViewById(R.id.button_sync_now);

        // Observe user data
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), this::updateUI);
        userViewModel.getIsLoggedIn().observe(getViewLifecycleOwner(), this::updateLoginButtons);

        // Setup click listeners
        buttonLogin.setOnClickListener(v -> {
            // In a real app, this would launch a login screen
            // For simplicity, we'll just do a mock login
            userViewModel.login("user123", "Demo User", "user@example.com");
            Toast.makeText(getContext(), "Logged in as Demo User", Toast.LENGTH_SHORT).show();
        });

        buttonLogout.setOnClickListener(v -> {
            userViewModel.logout();
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
        });

        buttonSyncNow.setOnClickListener(v -> {
            User currentUser = userViewModel.getCurrentUser().getValue();
            if (currentUser != null && currentUser.isSyncEnabled()) {
                scheduleViewModel.syncWithCloud(currentUser.getUserId());
                Toast.makeText(getContext(), "Syncing data...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Sync is disabled", Toast.LENGTH_SHORT).show();
            }
        });

        switchSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userViewModel.updateSyncPreference(isChecked);
            Toast.makeText(getContext(), isChecked ? "Sync enabled" : "Sync disabled", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUI(User user) {
        if (user != null) {
            textViewUsername.setText(user.getUsername());
            textViewEmail.setText(user.getEmail());
            switchSync.setChecked(user.isSyncEnabled());
            switchSync.setEnabled(true);
            buttonSyncNow.setEnabled(user.isSyncEnabled());
        } else {
            textViewUsername.setText("Not logged in");
            textViewEmail.setText("");
            switchSync.setChecked(false);
            switchSync.setEnabled(false);
            buttonSyncNow.setEnabled(false);
        }
    }

    private void updateLoginButtons(Boolean isLoggedIn) {
        if (isLoggedIn) {
            buttonLogin.setVisibility(View.GONE);
            buttonLogout.setVisibility(View.VISIBLE);
        } else {
            buttonLogin.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.GONE);
        }
    }
}