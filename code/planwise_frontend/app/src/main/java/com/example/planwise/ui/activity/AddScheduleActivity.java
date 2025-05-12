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
    private Button buttonAddTag;

    private Calendar selectedDateTime = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    // 预设标签数组
    private String[] defaultCategories = {"个人", "学习", "工作", "健康", "购物", "社交", "家庭", "休闲"};

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
        buttonAddTag = findViewById(R.id.button_add_tag);

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

        // 添加新标签按钮点击事件
        buttonAddTag.setOnClickListener(v -> {
            showAddTagDialog();
        });

        // Populate categories
        loadCategories();

        // Save button click listener
        buttonSave.setOnClickListener(v -> saveSchedule());
    }

    private void loadCategories() {
        chipGroupCategory.removeAllViews();

        // 先加载预设标签
        for (String category : defaultCategories) {
            addCategoryChip(category);
        }

        // 然后观察数据库中的标签
        viewModel.getAllCategories().observe(this, categories -> {
            // 添加数据库中有的但预设标签中没有的标签
            if (categories != null) {
                for (String category : categories) {
                    boolean exists = false;
                    for (String defaultCategory : defaultCategories) {
                        if (defaultCategory.equals(category)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        addCategoryChip(category);
                    }
                }
            }
        });
    }

    private void addCategoryChip(String category) {
        // 检查是否已经存在
        for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
            Chip existingChip = (Chip) chipGroupCategory.getChildAt(i);
            if (existingChip.getText().toString().equals(category)) {
                return; // 如果已存在，就不添加了
            }
        }

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
        String category = "其他";
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

    // 显示添加新标签的对话框
    private void showAddTagDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("添加新标签");

        // 设置输入框
        final EditText input = new EditText(this);
        input.setHint("请输入标签名称");
        builder.setView(input);

        // 设置按钮
        builder.setPositiveButton("添加", (dialog, which) -> {
            String newTag = input.getText().toString().trim();
            if (!newTag.isEmpty()) {
                // 添加新标签
                addCategoryChip(newTag);
                // 自动选中新标签
                for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupCategory.getChildAt(i);
                    if (chip.getText().toString().equals(newTag)) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
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