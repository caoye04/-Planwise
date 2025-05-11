package com.example.planwise.ui.activity;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
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

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_detail);

        // Enable back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Schedule Details");

        // Initialize views
        textViewTitle = findViewById(R.id.text_view_title);
        textViewDescription = findViewById(R.id.text_view_description);
        textViewDateTime = findViewById(R.id.text_view_date_time);
        textViewLocation = findViewById(R.id.text_view_location);
        textViewCategory = findViewById(R.id.text_view_category);
        checkBoxCompleted = findViewById(R.id.checkbox_completed);
        textViewAiSuggestion = findViewById(R.id.text_view_ai_suggestion);

        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        // Get schedule ID from intent
        long scheduleId = getIntent().getLongExtra("schedule_id", -1);
        if (scheduleId == -1) {
            Toast.makeText(this, "Error: Schedule not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Observe schedule details
        viewModel.getScheduleById(scheduleId).observe(this, schedule -> {
            if (schedule != null) {
                currentSchedule = schedule;
                displayScheduleDetails(schedule);
                generateAiSuggestion(schedule);
            } else {
                Toast.makeText(this, "Schedule not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Setup checkbox listener
        checkBoxCompleted.setOnClickListener(v -> {
            if (currentSchedule != null) {
                viewModel.toggleScheduleCompleted(currentSchedule);
            }
        });
    }

    private void displayScheduleDetails(Schedule schedule) {
        textViewTitle.setText(schedule.getTitle());

        // Description (show "No description" if empty)
        if (schedule.getDescription() != null && !schedule.getDescription().isEmpty()) {
            textViewDescription.setText(schedule.getDescription());
        } else {
            textViewDescription.setText("No description");
        }

        // Date and time
        if (schedule.getScheduledDate() != null) {
            textViewDateTime.setText(dateTimeFormat.format(schedule.getScheduledDate()));
        } else {
            textViewDateTime.setText("No date specified");
        }

        // Location (show "No location" if empty)
        if (schedule.getLocation() != null && !schedule.getLocation().isEmpty()) {
            textViewLocation.setText(schedule.getLocation());
        } else {
            textViewLocation.setText("No location");
        }

        textViewCategory.setText(schedule.getCategory());
        checkBoxCompleted.setChecked(schedule.isCompleted());
    }

    private void generateAiSuggestion(Schedule schedule) {
        // This would typically call an AI API or use a local model
        // For now, we'll use some simple heuristics

        String suggestion;
        String title = schedule.getTitle().toLowerCase();
        String category = schedule.getCategory();

        if (title.contains("meeting") || title.contains("appointment")) {
            suggestion = "Prepare necessary documents 15 minutes before your meeting.";
        } else if (category.equals("Study")) {
            suggestion = "Consider using the Pomodoro technique: 25 minutes of focus followed by a 5-minute break.";
        } else if (category.equals("Work")) {
            suggestion = "Try to complete this task during your peak productivity hours.";
        } else if (title.contains("exercise") || title.contains("workout")) {
            suggestion = "Stay hydrated and prepare your workout clothes in advance.";
        } else {
            suggestion = "Break this task into smaller steps for better productivity.";
        }

        textViewAiSuggestion.setText(suggestion);
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
            Toast.makeText(this, "Edit functionality", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDeleteSchedule();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteSchedule() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Schedule")
                .setMessage("Are you sure you want to delete this schedule?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (currentSchedule != null) {
                        viewModel.deleteSchedule(currentSchedule);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}