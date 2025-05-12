package com.example.planwise.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.planwise.R;
import com.example.planwise.data.model.Schedule;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ScheduleDetailActivity extends AppCompatActivity {

    private ScheduleViewModel viewModel;
    private Schedule currentSchedule;

    private TextView textViewTitle;
    private TextView textViewDescription;
    private TextView textViewDateTime;
    private TextView textViewLocation;
    private TextView textViewCategory;
    private CheckBox checkBoxCompleted;
    private TextView textViewAiSuggestion;
    private Button buttonGetAiSuggestion;
    private ProgressBar progressAiSuggestion;

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_detail);

        // Enable back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("日程详情");

        // Initialize views
        textViewTitle = findViewById(R.id.text_view_title);
        textViewDescription = findViewById(R.id.text_view_description);
        textViewDateTime = findViewById(R.id.text_view_date_time);
        textViewLocation = findViewById(R.id.text_view_location);
        textViewCategory = findViewById(R.id.text_view_category);
        checkBoxCompleted = findViewById(R.id.checkbox_completed);
        textViewAiSuggestion = findViewById(R.id.text_view_ai_suggestion);
        buttonGetAiSuggestion = findViewById(R.id.button_get_ai_suggestion);
        progressAiSuggestion = findViewById(R.id.progress_ai_suggestion);

        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        // Get schedule ID from intent
        long scheduleId = getIntent().getLongExtra("schedule_id", -1);
        if (scheduleId == -1) {
            Toast.makeText(this, "错误：未找到日程", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Observe schedule details
        viewModel.getScheduleById(scheduleId).observe(this, schedule -> {
            if (schedule != null) {
                currentSchedule = schedule;
                displayScheduleDetails(schedule);
            } else {
                Toast.makeText(this, "未找到日程", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Setup checkbox listener
        checkBoxCompleted.setOnClickListener(v -> {
            if (currentSchedule != null) {
                viewModel.toggleScheduleCompleted(currentSchedule);
            }
        });

        // Setup AI suggestion button
        buttonGetAiSuggestion.setOnClickListener(v -> {
            if (currentSchedule != null) {
                // 显示加载提示
                textViewAiSuggestion.setText("正在获取 AI 小助手的建议，请稍等...");
                // 调用 AI 建议方法
                viewModel.getAiSuggestion(currentSchedule);
            }
        });

        // Observe AI suggestion loading state
        viewModel.getIsLoadingAiSuggestion().observe(this, isLoading -> {
            progressAiSuggestion.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            buttonGetAiSuggestion.setEnabled(!isLoading);
            if (isLoading) {
                buttonGetAiSuggestion.setText("正在获取建议...");
            } else {
                buttonGetAiSuggestion.setText("问问PlanWiseAI的建议吧！");
            }
        });

        // Observe AI suggestion
        viewModel.getAiSuggestion().observe(this, suggestion -> {
            if (suggestion != null && !suggestion.isEmpty()) {
                textViewAiSuggestion.setText(suggestion);
            }
        });
    }

    private void displayScheduleDetails(Schedule schedule) {
        textViewTitle.setText(schedule.getTitle());

        // Description (show "No description" if empty)
        if (schedule.getDescription() != null && !schedule.getDescription().isEmpty()) {
            textViewDescription.setText(schedule.getDescription());
        } else {
            textViewDescription.setText("暂无描述");
        }

        // Date and time
        if (schedule.getScheduledDate() != null) {
            textViewDateTime.setText(dateTimeFormat.format(schedule.getScheduledDate()));
        } else {
            textViewDateTime.setText("未指定日期");
        }

        // Location (show "No location" if empty)
        if (schedule.getLocation() != null && !schedule.getLocation().isEmpty()) {
            textViewLocation.setText(schedule.getLocation());
        } else {
            textViewLocation.setText("未指定地点");
        }

        textViewCategory.setText(schedule.getCategory());
        checkBoxCompleted.setChecked(schedule.isCompleted());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit) {
            // Intent to edit schedule (not implemented in this code sample)
            Toast.makeText(this, "编辑功能", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDeleteSchedule();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteSchedule() {
        new AlertDialog.Builder(this)
                .setTitle("删除日程")
                .setMessage("确定要删除这个日程吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    if (currentSchedule != null) {
                        viewModel.deleteSchedule(currentSchedule);
                        finish();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}