package com.example.planwise.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.example.planwise.R;
import com.example.planwise.data.model.Schedule;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddScheduleActivity extends AppCompatActivity {

    private ScheduleViewModel viewModel;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextDate;
    private EditText editTextTime;
    private EditText editTextLocation;
    private ChipGroup chipGroupCategory;
    private Button buttonSave;

    private Calendar selectedDateTime = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        // 安全地设置 ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("新建待办");
        }

        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        // Initialize views
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextDate = findViewById(R.id.edit_text_date);
        editTextTime = findViewById(R.id.edit_text_time);
        editTextLocation = findViewById(R.id.edit_text_location);
        chipGroupCategory = findViewById(R.id.chip_group_category);
        buttonSave = findViewById(R.id.button_save);

        // Initialize date and time
        updateDateText();
        updateTimeText();

        // Setup date picker dialog
        editTextDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(selectedDateTime.getTimeInMillis())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDateTime.setTimeInMillis(selection);
                updateDateText();
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        // Setup time picker dialog
        editTextTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(selectedDateTime.get(Calendar.HOUR_OF_DAY))
                    .setMinute(selectedDateTime.get(Calendar.MINUTE))
                    .setTitleText("Select time")
                    .build();

            timePicker.addOnPositiveButtonClickListener(v1 -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                selectedDateTime.set(Calendar.MINUTE, timePicker.getMinute());
                updateTimeText();
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });

        // Populate categories
        viewModel.getAllCategories().observe(this, categories -> {
            chipGroupCategory.removeAllViews();

            // Add default categories if empty
            if (categories == null || categories.isEmpty()) {
                addCategoryChip("Work");
                addCategoryChip("Personal");
                addCategoryChip("Study");
                addCategoryChip("Shopping");
            } else {
                for (String category : categories) {
                    addCategoryChip(category);
                }
            }
        });

        // Save button click listener
        buttonSave.setOnClickListener(v -> saveSchedule());
    }

    private void addCategoryChip(String category) {
        Chip chip = new Chip(this);
        chip.setText(category);
        chip.setCheckable(true);
        chipGroupCategory.addView(chip);
    }

    private void updateDateText() {
        editTextDate.setText(dateFormat.format(selectedDateTime.getTime()));
    }

    private void updateTimeText() {
        editTextTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void saveSchedule() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        if (title.isEmpty()) {
            editTextTitle.setError("Title cannot be empty");
            return;
        }

        // Get selected category
        String category = "Other";
        int checkedChipId = chipGroupCategory.getCheckedChipId();
        if (checkedChipId != -1) {
            Chip selectedChip = findViewById(checkedChipId);
            category = selectedChip.getText().toString();
        }

        // Create and save schedule
        Schedule schedule = new Schedule(
                title,
                description,
                selectedDateTime.getTime(),
                location,
                category,
                false
        );

        viewModel.insertSchedule(schedule);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}