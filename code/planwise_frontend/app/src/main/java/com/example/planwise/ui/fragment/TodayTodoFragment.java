package com.example.planwise.ui.fragment;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
    private TextView textViewFilterHint; // 添加筛选提示视图引用

    private List<Schedule> allSchedules = new ArrayList<>();
    private int currentFilter = FILTER_ALL;

    // Filter constants
    private static final int FILTER_ALL = 0;
    private static final int FILTER_INCOMPLETE = 1;
    private static final int FILTER_COMPLETED = 2;
    private static final int FILTER_TIME = 3;
    private static final int FILTER_TAG = 4;
    // 新增的快捷筛选类型
    private static final int FILTER_TODAY = 5;
    private static final int FILTER_THREE_DAYS = 6;
    private static final int FILTER_THIS_WEEK = 7;

    private Date filterStartDate = null;
    private Date filterEndDate = null;
    private String filterTag = null;
    
    // 新的筛选状态变量
    private int baseFilter = FILTER_ALL; 

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

        // 初始化筛选提示视图
        textViewFilterHint = view.findViewById(R.id.text_view_filter_hint);

        // Setup filter chips
        ChipGroup chipGroupFilter = view.findViewById(R.id.chip_group_filter);
        Chip chipAll = view.findViewById(R.id.chip_filter_all);
        Chip chipIncomplete = view.findViewById(R.id.chip_filter_incomplete);
        Chip chipCompleted = view.findViewById(R.id.chip_filter_completed);

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_filter_all) {
                currentFilter = FILTER_ALL;
                baseFilter = FILTER_ALL; // 重置基础筛选
                updateFilterHint(null); // 清除筛选提示
            } else if (checkedId == R.id.chip_filter_incomplete) {
                currentFilter = FILTER_INCOMPLETE;
                baseFilter = FILTER_INCOMPLETE; // 重置基础筛选
                updateFilterHint("当前筛选：未完成"); // 更新筛选提示
            } else if (checkedId == R.id.chip_filter_completed) {
                currentFilter = FILTER_COMPLETED;
                baseFilter = FILTER_COMPLETED; // 重置基础筛选
                updateFilterHint("当前筛选：已完成"); // 更新筛选提示
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

    // 添加更新筛选提示的方法
    private void updateFilterHint(String hintText) {
        if (hintText == null || hintText.isEmpty()) {
            textViewFilterHint.setVisibility(View.GONE);
        } else {
            textViewFilterHint.setText(hintText);
            textViewFilterHint.setVisibility(View.VISIBLE);
        }
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
                        if (baseFilter == FILTER_INCOMPLETE) {
                            shouldInclude = shouldInclude && !schedule.isCompleted();
                        } else if (baseFilter == FILTER_COMPLETED) {
                            shouldInclude = shouldInclude && schedule.isCompleted();
                        }
                    }
                    break;
                case FILTER_TODAY:
                    // 检查是否是今天的待办
                    shouldInclude = isScheduleToday(schedule);
                    break;
                case FILTER_THREE_DAYS:
                    // 检查是否是近三天的待办
                    shouldInclude = isScheduleWithinDays(schedule, 3);
                    break;
                case FILTER_THIS_WEEK:
                    // 检查是否是本周的待办
                    shouldInclude = isScheduleThisWeek(schedule);
                    break;
            }

            if (shouldInclude) {
                filteredList.add(schedule);
            }
        }

        adapter.submitList(filteredList);
    }

    // 检查日程是否是今天的
    private boolean isScheduleToday(Schedule schedule) {
        if (schedule.getScheduledDate() == null) return false;

        Calendar scheduleCalendar = Calendar.getInstance();
        scheduleCalendar.setTime(schedule.getScheduledDate());

        Calendar todayCalendar = Calendar.getInstance();

        return scheduleCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                scheduleCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR);
    }

    // 检查日程是否在指定天数内
    private boolean isScheduleWithinDays(Schedule schedule, int days) {
        if (schedule.getScheduledDate() == null) return false;

        // 获取当前时间
        Calendar currentCalendar = Calendar.getInstance();
        // 设置为今天的开始时间
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);

        // 获取未来的截止时间
        Calendar futureCalendar = Calendar.getInstance();
        futureCalendar.add(Calendar.DAY_OF_YEAR, days);
        futureCalendar.set(Calendar.HOUR_OF_DAY, 23);
        futureCalendar.set(Calendar.MINUTE, 59);
        futureCalendar.set(Calendar.SECOND, 59);
        futureCalendar.set(Calendar.MILLISECOND, 999);

        return !schedule.getScheduledDate().before(currentCalendar.getTime()) &&
                !schedule.getScheduledDate().after(futureCalendar.getTime());
    }

    // 检查日程是否在本周内
    private boolean isScheduleThisWeek(Schedule schedule) {
        if (schedule.getScheduledDate() == null) return false;

        Calendar scheduleCalendar = Calendar.getInstance();
        scheduleCalendar.setTime(schedule.getScheduledDate());

        Calendar currentCalendar = Calendar.getInstance();

        // 检查是否是同一年的同一周
        return scheduleCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                scheduleCalendar.get(Calendar.WEEK_OF_YEAR) == currentCalendar.get(Calendar.WEEK_OF_YEAR);
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

        // 今晚的任务
        calendar.add(Calendar.HOUR, 4);
        Schedule schedule1 = new Schedule(
                "复习数据结构",
                "准备明天的数据结构期中考试，重点复习树和图的算法，整理课堂笔记和习题",
                calendar.getTime(),
                "宿舍",
                "学习",
                false
        );

        // 明天早上
        calendar.add(Calendar.HOUR, 8);
        Schedule schedule2 = new Schedule(
                "提交Java项目代码",
                "完成代码评审修改，更新文档，提交到GitLab，发送邮件通知组长",
                calendar.getTime(),
                "实验室",
                "工作",
                false
        );

        // 明天下午
        calendar.add(Calendar.HOUR, 6);
        Schedule schedule3 = new Schedule(
                "算法课程作业",
                "完成第4章动态规划习题，整理课后练习，准备下周的课堂展示",
                calendar.getTime(),
                "图书馆",
                "学习",
                false
        );

        // 后天上午
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Schedule schedule4 = new Schedule(
                "项目组周会",
                "汇报本周进度，讨论技术难点，确定下周任务分工",
                calendar.getTime(),
                "线上会议",
                "工作",
                false
        );

        // 后天下午
        calendar.add(Calendar.HOUR, 5);
        Schedule schedule5 = new Schedule(
                "健身计划",
                "力量训练日：深蹲4×12, 硬拉3×8, 核心训练15分钟",
                calendar.getTime(),
                "健身房",
                "健康",
                false
        );

        // 3天后
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Schedule schedule6 = new Schedule(
                "准备英语演讲",
                "准备下周的商务英语演讲，主题：可持续发展，准备PPT和讲稿",
                calendar.getTime(),
                "自习室",
                "学习",
                false
        );

        // 4天后
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Schedule schedule7 = new Schedule(
                "家庭聚餐",
                "奶奶生日聚餐，提前订蛋糕，准备礼物，联系其他家人",
                calendar.getTime(),
                "家里",
                "家庭",
                false
        );

        // 5天后
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Schedule schedule8 = new Schedule(
                "项目需求评审",
                "参加新项目需求评审会，准备技术可行性分析报告",
                calendar.getTime(),
                "会议室",
                "工作",
                false
        );

        // 一周后
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        Schedule schedule9 = new Schedule(
                "数据库实验",
                "完成数据库高级应用实验，准备实验报告和演示文档",
                calendar.getTime(),
                "实验室",
                "学习",
                false
        );

        // 10天后
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        Schedule schedule10 = new Schedule(
                "月度购物",
                "采购生活用品和食材，关注优惠活动，记得带购物清单",
                calendar.getTime(),
                "超市",
                "购物",
                false
        );

        // 两周后
        calendar.add(Calendar.DAY_OF_MONTH, 4);
        Schedule schedule11 = new Schedule(
                "项目验收报告",
                "整理项目文档，准备验收演示，完成结项报告",
                calendar.getTime(),
                "公司",
                "工作",
                false
        );

        // 半个月后
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Schedule schedule12 = new Schedule(
                "朋友聚会",
                "老友聚会，地点：火锅店，组织活动和游戏",
                calendar.getTime(),
                "restaurants",
                "社交",
                false
        );

        // 三周后
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        Schedule schedule13 = new Schedule(
                "软件工程答辩",
                "准备项目答辩PPT，整理源代码，进行演示排练",
                calendar.getTime(),
                "实验室",
                "学习",
                false
        );

        // 一个月后
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        Schedule schedule14 = new Schedule(
                "休闲旅行规划",
                "规划端午假期出行，订票订房，制定行程安排",
                calendar.getTime(),
                "家里",
                "休闲",
                false
        );

        // 将默认日程添加到数据库
        viewModel.insertSchedule(schedule1);
        viewModel.insertSchedule(schedule2);
        viewModel.insertSchedule(schedule3);
        viewModel.insertSchedule(schedule4);
        viewModel.insertSchedule(schedule5);
        viewModel.insertSchedule(schedule6);
        viewModel.insertSchedule(schedule7);
        viewModel.insertSchedule(schedule8);
        viewModel.insertSchedule(schedule9);
        viewModel.insertSchedule(schedule10);
        viewModel.insertSchedule(schedule11);
        viewModel.insertSchedule(schedule12);
        viewModel.insertSchedule(schedule13);
        viewModel.insertSchedule(schedule14);
    }

    /**
     * Shows dialog for advanced filtering options
     */
    private void showAdvancedFilterDialog() {
        String[] options = {"今日待办", "近三天待办", "本周待办", "按时间筛选", "按标签筛选", "清除筛选"};

        new AlertDialog.Builder(requireContext())
                .setTitle("选择筛选方式")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // 今日待办
                            applyTodayFilter();
                            break;
                        case 1: // 近三天待办
                            applyThreeDaysFilter();
                            break;
                        case 2: // 本周待办
                            applyThisWeekFilter();
                            break;
                        case 3: // 按时间筛选
                            showTimeFilterDialog();
                            break;
                        case 4: // 按标签筛选
                            showTagFilterDialog();
                            break;
                        case 5: // 清除筛选
                            clearAdvancedFilters();
                            break;
                    }
                })
                .show();
    }

    // 应用今日待办筛选
    private void applyTodayFilter() {
        currentFilter = FILTER_TODAY;
        // 重置筛选芯片选择
        ChipGroup chipGroup = requireView().findViewById(R.id.chip_group_filter);
        chipGroup.clearCheck();
        updateFilterHint("当前筛选：今日待办"); // 更新筛选提示
        applyFilter();
        Toast.makeText(requireContext(), "已筛选今日待办", Toast.LENGTH_SHORT).show();
    }

    // 应用近三天待办筛选
    private void applyThreeDaysFilter() {
        currentFilter = FILTER_THREE_DAYS;
        // 重置筛选芯片选择
        ChipGroup chipGroup = requireView().findViewById(R.id.chip_group_filter);
        chipGroup.clearCheck();
        updateFilterHint("当前筛选：近三天待办"); // 更新筛选提示
        applyFilter();
        Toast.makeText(requireContext(), "已筛选近三天待办", Toast.LENGTH_SHORT).show();
    }

    // 应用本周待办筛选
    private void applyThisWeekFilter() {
        currentFilter = FILTER_THIS_WEEK;
        // 重置筛选芯片选择
        ChipGroup chipGroup = requireView().findViewById(R.id.chip_group_filter);
        chipGroup.clearCheck();
        updateFilterHint("当前筛选：本周待办"); // 更新筛选提示
        applyFilter();
        Toast.makeText(requireContext(), "已筛选本周待办", Toast.LENGTH_SHORT).show();
    }

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

                    // 构建自定义时间筛选的提示文本
                    StringBuilder hintTextBuilder = new StringBuilder("当前筛选：");
                    if (startDate != null && endDate != null) {
                        hintTextBuilder.append(dateFormat.format(startDate))
                                .append(" 至 ")
                                .append(dateFormat.format(endDate));
                    } else if (startDate != null) {
                        hintTextBuilder.append(dateFormat.format(startDate))
                                .append(" 之后");
                    } else if (endDate != null) {
                        hintTextBuilder.append(dateFormat.format(endDate))
                                .append(" 之前");
                    }
                    updateFilterHint(hintTextBuilder.toString()); // 更新筛选提示

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

                    // 更新筛选提示
                    updateFilterHint("当前筛选：标签 - " + filterTag);

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

        // 清除筛选提示
        updateFilterHint(null);

        // Select the "All" chip
        ChipGroup chipGroup = requireView().findViewById(R.id.chip_group_filter);
        Chip chipAll = requireView().findViewById(R.id.chip_filter_all);
        chipAll.setChecked(true);

        applyFilter();

        Toast.makeText(requireContext(), "已清除筛选", Toast.LENGTH_SHORT).show();
    }
}