package com.example.planwise.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
            showLoginDialog();
        });

        buttonLogout.setOnClickListener(v -> {
            userViewModel.logout();
            Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
        });

        buttonSyncNow.setOnClickListener(v -> {
            User currentUser = userViewModel.getCurrentUser().getValue();
            if (currentUser != null && currentUser.isSyncEnabled()) {
                showSyncOptionsDialog(currentUser.getUserId());
            } else {
                Toast.makeText(getContext(), "同步功能已禁用", Toast.LENGTH_SHORT).show();
            }
        });

        switchSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userViewModel.updateSyncPreference(isChecked);
            Toast.makeText(getContext(), isChecked ? "同步已启用" : "同步已禁用", Toast.LENGTH_SHORT).show();
            buttonSyncNow.setEnabled(isChecked);
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
            textViewUsername.setText("未登录");
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

    private void showLoginDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_login, null);

        final EditText editTextUsername = dialogView.findViewById(R.id.edit_text_username);
        final EditText editTextEmail = dialogView.findViewById(R.id.edit_text_email);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("登录/注册", (dialogInterface, i) -> {
                    String username = editTextUsername.getText().toString().trim();
                    String email = editTextEmail.getText().toString().trim();

                    if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email)) {
                        Toast.makeText(getContext(), "用户名和邮箱不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 显示加载提示
                    Toast.makeText(getContext(), "正在登录...", Toast.LENGTH_SHORT).show();

                    // 尝试登录
                    userViewModel.login(email, username);
                })
                .setNegativeButton("取消", null)
                .create();

        dialog.show();
    }

    private void showSyncOptionsDialog(String userId) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_sync_options, null);

        Button buttonUploadToCloud = dialogView.findViewById(R.id.button_upload_to_cloud);
        Button buttonDownloadFromCloud = dialogView.findViewById(R.id.button_download_from_cloud);
        Button buttonBothWaySync = dialogView.findViewById(R.id.button_both_way_sync);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        buttonUploadToCloud.setOnClickListener(v -> {
            scheduleViewModel.uploadToCloud(userId);
            Toast.makeText(getContext(), "正在上传本地数据到云端...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        buttonDownloadFromCloud.setOnClickListener(v -> {
            scheduleViewModel.downloadFromCloud(userId);
            Toast.makeText(getContext(), "正在从云端下载数据...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        buttonBothWaySync.setOnClickListener(v -> {
            scheduleViewModel.syncWithCloud(userId);
            Toast.makeText(getContext(), "正在进行双向同步...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}