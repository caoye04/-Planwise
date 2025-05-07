package com.example.planwise.ui.fragment;


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
            }else{

                currentFilter = FILTER_ALL;
            }

            // Apply the filter to current data
            applyFilter();
        });
    // Observe today's schedules
    viewModel.getTodaySchedules().observe(getViewLifecycleOwner(), schedules -> {
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
}
