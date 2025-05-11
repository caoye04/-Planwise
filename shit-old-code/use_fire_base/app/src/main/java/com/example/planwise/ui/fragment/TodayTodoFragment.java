package com.example.planwise.ui.fragment;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.planwise.R;
import com.example.planwise.data.model.Schedule;
import com.example.planwise.ui.activity.AddScheduleActivity;
import com.example.planwise.ui.activity.ScheduleDetailActivity;
import com.example.planwise.ui.adapter.ScheduleAdapter;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;

import java.util.List;
import java.util.ArrayList;

public class TodayTodoFragment extends Fragment implements ScheduleAdapter.OnScheduleListener {

    private ScheduleViewModel viewModel;
    private ScheduleAdapter adapter;

    private List<Schedule> allSchedules = new ArrayList<>();
    private int currentFilter = FILTER_ALL;

    // Filter constants
    private static final int FILTER_ALL = 0;
    private static final int FILTER_INCOMPLETE = 1;
    private static final int FILTER_COMPLETED = 2;
    private static final int FILTER_TIME = 3;
    private static final int FILTER_TAG = 4;

    private Date filterStartDate = null;
    private Date filterEndDate = null;
    private String filterTag = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today_todo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ScheduleViewModel.class);

        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScheduleAdapter(this);
        recyclerView.setAdapter(adapter);

// Setup filter chips
        ChipGroup chipGroupFilter = view.findViewById(R.id.chip_group_filter);
        Chip chipAll = view.findViewById(R.id.chip_filter_all);
        Chip chipIncomplete = view.findViewById(R.id.chip_filter_incomplete);
        Chip chipCompleted = view.findViewById(R.id.chip_filter_completed);

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_filter_all) {
                currentFilter = FILTER_ALL;
            } else if (checkedId == R.id.chip_filter_incomplete) {
                currentFilter = FILTER_INCOMPLETE;
            } else if (checkedId == R.id.chip_filter_completed) {
                currentFilter = FILTER_COMPLETED;
            }

            // Apply the filter to current data
            applyFilter();
        });
        // Setup advanced filter button
        ImageButton btnAdvancedFilter = view.findViewById(R.id.btn_advanced_filter);
        btnAdvancedFilter.setOnClickListener(v -> {
            showAdvancedFilterDialog();
        });
        // Observe today's schedules
        viewModel.getAllSchedules().observe(getViewLifecycleOwner(), schedules -> {
            allSchedules = schedules != null ? schedules : new ArrayList<>();

            // Apply current filter to the new data
            applyFilter();

            // 只有当列表为空时才添加默认日程
            if (schedules == null || schedules.isEmpty()) {
                addDefaultSchedules();
            }
        });
        // Setup swipe delete functionality
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Schedule schedule = adapter.getScheduleAt(position);
                viewModel.deleteSchedule(schedule);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

        // Setup FAB for adding new schedules
        FloatingActionButton add_todo_btn = view.findViewById(R.id.fab_add);
        add_todo_btn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddScheduleActivity.class);
            startActivity(intent);
        });
    }

    private void applyFilter() {
        List<Schedule> filteredList = new ArrayList<>();

        for (Schedule schedule : allSchedules) {
            boolean shouldInclude = false;

            switch (currentFilter) {
                case FILTER_ALL:
                    shouldInclude = true;
                    break;
                case FILTER_INCOMPLETE:
                    shouldInclude = !schedule.isCompleted();
                    break;
                case FILTER_COMPLETED:
                    shouldInclude = schedule.isCompleted();
                    break;
                case FILTER_TIME:
                    // Check if schedule is within time range
                    Date scheduleDate = schedule.getScheduledDate();

                    // If only start date is specified
                    if (filterStartDate != null && filterEndDate == null) {
                        shouldInclude = !scheduleDate.before(filterStartDate);
                    }
                    // If only end date is specified
                    else if (filterStartDate == null && filterEndDate != null) {
                        shouldInclude = !scheduleDate.after(filterEndDate);
                    }
                    // If both dates are specified
                    else if (filterStartDate != null && filterEndDate != null) {
                        shouldInclude = !scheduleDate.before(filterStartDate) && !scheduleDate.after(filterEndDate);
                    }
                    break;
                case FILTER_TAG:
                    // Check if schedule matches the tag
                    if (filterTag != null && schedule.getCategory() != null) {
                        shouldInclude = schedule.getCategory().equals(filterTag);
                    }
                    break;
            }

            if (shouldInclude) {
                filteredList.add(schedule);
            }
        }

        adapter.submitList(filteredList);
    }

    @Override
    public void onScheduleClick(Schedule schedule) {
        // Navigate to schedule detail
        Intent intent = new Intent(getActivity(), ScheduleDetailActivity.class);
        intent.putExtra("schedule_id", schedule.getId());
        startActivity(intent);
    }

    @Override
    public void onCompletedToggle(Schedule schedule) {
        viewModel.toggleScheduleCompleted(schedule);
    }

    private void addDefaultSchedules() {
        // 获取当前日期
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.util.Date today = calendar.getTime();

        // 创建3条默认日程，使用完整构造函数
        Schedule schedule1 = new Schedule(
                "完成Android作业",
                "完成PlanWise应用开发",
                today,
                "实验室", // 位置
                "学习", // 分类
                false // 完成状态
        );

        Schedule schedule2 = new Schedule(
                "阅读专业书籍",
                "阅读一小时专业相关书籍",
                today,
                "图书馆",
                "学习",
                false
        );

        Schedule schedule3 = new Schedule(
                "健身锻炼",
                "进行30分钟有氧运动",
                today,
                "健身房",
                "个人",
                false
        );

        // 将默认日程添加到数据库
        viewModel.insertSchedule(schedule1);
        viewModel.insertSchedule(schedule2);
        viewModel.insertSchedule(schedule3);
    }
    /**
     * Shows dialog for advanced filtering options
     */
    private void showAdvancedFilterDialog() {
        String[] options = {"按时间筛选", "按标签筛选", "清除筛选"};

        new AlertDialog.Builder(requireContext())
                .setTitle("选择筛选方式")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // 按时间筛选
                            showTimeFilterDialog();
                            break;
                        case 1: // 按标签筛选
                            showTagFilterDialog();
                            break;
                        case 2: // 清除筛选
                            clearAdvancedFilters();
                            break;
                    }
                })
                .show();
    }

    /**
     * Shows dialog for time-based filtering
     */
    /**
     * Shows dialog for time-based filtering
     */
    private void showTimeFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_time_filter, null);

        EditText etStartDate = dialogView.findViewById(R.id.et_start_date);
        EditText etStartTime = dialogView.findViewById(R.id.et_start_time);
        EditText etEndDate = dialogView.findViewById(R.id.et_end_date);
        EditText etEndTime = dialogView.findViewById(R.id.et_end_time);

        // 初始化日历对象用于保存选择的时间
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();

        // 默认时间格式化器
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // 设置日期选择器 - 开始日期
        etStartDate.setOnClickListener(v -> {
            showDatePicker((view, year, month, dayOfMonth) -> {
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, month);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etStartDate.setText(dateFormat.format(startCalendar.getTime()));
            });
        });

        // 设置日期选择器 - 结束日期
        etEndDate.setOnClickListener(v -> {
            showDatePicker((view, year, month, dayOfMonth) -> {
                endCalendar.set(Calendar.YEAR, year);
                endCalendar.set(Calendar.MONTH, month);
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etEndDate.setText(dateFormat.format(endCalendar.getTime()));
            });
        });

        // 设置时间选择器 - 开始时间
        etStartTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            new TimePickerDialog(
                    requireContext(),
                    (view, hourOfDay, minute1) -> {
                        startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startCalendar.set(Calendar.MINUTE, minute1);
                        startCalendar.set(Calendar.SECOND, 0);
                        startCalendar.set(Calendar.MILLISECOND, 0);
                        etStartTime.setText(timeFormat.format(startCalendar.getTime()));
                    },
                    hour,
                    minute,
                    true // 24小时制
            ).show();
        });

// 设置时间选择器 - 结束时间
        etEndTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            new TimePickerDialog(
                    requireContext(),
                    (view, hourOfDay, minute1) -> {
                        endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        endCalendar.set(Calendar.MINUTE, minute1);
                        endCalendar.set(Calendar.SECOND, 59);
                        endCalendar.set(Calendar.MILLISECOND, 999);
                        etEndTime.setText(timeFormat.format(endCalendar.getTime()));
                    },
                    hour,
                    minute,
                    true // 24小时制
            ).show();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("按时间筛选")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    // 获取开始和结束日期时间
                    Date startDate = null;
                    Date endDate = null;

                    // 如果开始日期已设置
                    if (!etStartDate.getText().toString().isEmpty()) {
                        startDate = startCalendar.getTime();
                    }

                    // 如果结束日期已设置
                    if (!etEndDate.getText().toString().isEmpty()) {
                        endDate = endCalendar.getTime();
                    }

                    // 应用时间筛选
                    filterStartDate = startDate;
                    filterEndDate = endDate;
                    currentFilter = FILTER_TIME;

                    // 重置筛选芯片选择
                    ChipGroup chipGroup = requireView().findViewById(R.id.chip_group_filter);
                    chipGroup.clearCheck();

                    applyFilter();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示时间选择器
     */
    private void showTimePicker(TimePickerDialog.OnTimeSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 创建24小时制的时间选择器
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                listener,
                hour,
                minute,
                true // 24小时制
        );

        timePickerDialog.show();
    }
    /**
     * Shows DatePickerDialog with the given listener
     */
    private void showDatePicker(DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                listener,
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    /**
     * Shows dialog for tag-based filtering
     */
    private void showTagFilterDialog() {
        // Get all unique tags from schedules
        Set<String> tags = new HashSet<>();
        for (Schedule schedule : allSchedules) {
            if (schedule.getCategory() != null && !schedule.getCategory().isEmpty()) {
                tags.add(schedule.getCategory());
            }
        }

        if (tags.isEmpty()) {
            Toast.makeText(requireContext(), "没有可用的标签", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] tagArray = tags.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("选择标签")
                .setItems(tagArray, (dialog, which) -> {
                    filterTag = tagArray[which];
                    currentFilter = FILTER_TAG;

                    // Reset chips selection
                    ChipGroup chipGroup = requireView().findViewById(R.id.chip_group_filter);
                    chipGroup.clearCheck();

                    applyFilter();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * Clear all advanced filters
     */
    private void clearAdvancedFilters() {
        filterStartDate = null;
        filterEndDate = null;
        filterTag = null;

        // Reset to show all tasks
        currentFilter = FILTER_ALL;

        // Select the "All" chip
        ChipGroup chipGroup = requireView().findViewById(R.id.chip_group_filter);
        Chip chipAll = requireView().findViewById(R.id.chip_filter_all);
        chipAll.setChecked(true);

        applyFilter();

        Toast.makeText(requireContext(), "已清除筛选", Toast.LENGTH_SHORT).show();
    }
}
