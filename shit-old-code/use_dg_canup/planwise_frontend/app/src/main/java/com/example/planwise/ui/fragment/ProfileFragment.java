package com.example.planwise.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.planwise.R;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;

public class ProfileFragment extends Fragment {

    private ScheduleViewModel scheduleViewModel;

    private TextView textViewUsername;
    private Button buttonUploadToCloud;
    private Button buttonDownloadFromCloud;
    private ProgressBar progressBarSync;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup ViewModel
        scheduleViewModel = new ViewModelProvider(requireActivity()).get(ScheduleViewModel.class);

        // Initialize views
        textViewUsername = view.findViewById(R.id.text_view_username);
        textViewUsername.setText("PlanWiser"); // 固定用户名

        buttonUploadToCloud = view.findViewById(R.id.button_upload_to_cloud);
        buttonDownloadFromCloud = view.findViewById(R.id.button_download_from_cloud);
        progressBarSync = view.findViewById(R.id.progress_bar_sync);

        // 设置云同步按钮点击事件
        buttonUploadToCloud.setOnClickListener(v -> {
            scheduleViewModel.uploadToCloud();
            Toast.makeText(getContext(), "正在上传数据到云端...", Toast.LENGTH_SHORT).show();
        });

        buttonDownloadFromCloud.setOnClickListener(v -> {
            scheduleViewModel.downloadFromCloud();
            Toast.makeText(getContext(), "正在从云端下载数据...", Toast.LENGTH_SHORT).show();
        });

        // 观察同步状态
        scheduleViewModel.getIsSyncing().observe(getViewLifecycleOwner(), isSyncing -> {
            progressBarSync.setVisibility(isSyncing ? View.VISIBLE : View.GONE);
            buttonUploadToCloud.setEnabled(!isSyncing);
            buttonDownloadFromCloud.setEnabled(!isSyncing);
        });

        scheduleViewModel.getSyncErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), "同步错误: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}