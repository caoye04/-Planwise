package com.example.planwise.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.planwise.R;
import com.example.planwise.data.api.ApiClient;
import com.example.planwise.data.repository.ScheduleRepository;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;

public class ProfileFragment extends Fragment {

    private ScheduleViewModel scheduleViewModel;
    private TextView textViewUsername;
    private TextView textViewEmail;
    private Button buttonLocalToCloud;
    private Button buttonCloudToLocal;
    private Button buttonSetServerIp;
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
        textViewEmail = view.findViewById(R.id.text_view_email);
        buttonLocalToCloud = view.findViewById(R.id.button_local_to_cloud);
        buttonCloudToLocal = view.findViewById(R.id.button_cloud_to_local);
        buttonSetServerIp = view.findViewById(R.id.button_set_server_ip);
        progressBarSync = view.findViewById(R.id.progress_bar_sync);

        // Set default user
        textViewUsername.setText("Planwiser");
        textViewEmail.setText("用户默认");

        // Setup click listeners for sync buttons
        buttonLocalToCloud.setOnClickListener(v -> syncLocalToCloud());
        buttonCloudToLocal.setOnClickListener(v -> syncCloudToLocal());
        buttonSetServerIp.setOnClickListener(v -> showIpSettingDialog());

        // Observe syncing status
        scheduleViewModel.getIsSyncing().observe(getViewLifecycleOwner(), isSyncing -> {
            if (isSyncing) {
                progressBarSync.setVisibility(View.VISIBLE);
                buttonLocalToCloud.setEnabled(false);
                buttonCloudToLocal.setEnabled(false);
            } else {
                progressBarSync.setVisibility(View.GONE);
                buttonLocalToCloud.setEnabled(true);
                buttonCloudToLocal.setEnabled(true);
            }
        });
    }

    private void syncLocalToCloud() {
        scheduleViewModel.syncLocalToCloud(new ScheduleRepository.OnSyncCompleteListener() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void syncCloudToLocal() {
        new AlertDialog.Builder(requireContext())
                .setTitle("确认同步")
                .setMessage("从云端同步将覆盖本地所有数据，确定要继续吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    scheduleViewModel.syncCloudToLocal(new ScheduleRepository.OnSyncCompleteListener() {
                        @Override
                        public void onSyncComplete(boolean success, String message) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showIpSettingDialog() {
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("例如: 192.168.1.100:8000");

        new AlertDialog.Builder(requireContext())
                .setTitle("设置服务器IP和端口")
                .setMessage("请输入Django服务器的IP地址和端口")
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    String ipAndPort = input.getText().toString().trim();
                    if (!ipAndPort.isEmpty()) {
                        // 设置服务器IP和端口
                        ApiClient.setBaseUrl("http://" + ipAndPort + "/");
                        Toast.makeText(getContext(), "服务器地址已设置为: " + ipAndPort, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}