package com.example.planwise.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planwise.R;
import com.example.planwise.data.model.Schedule;
import com.example.planwise.ui.activity.ScheduleDetailActivity;
import com.example.planwise.ui.adapter.ScheduleAdapter;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;

import java.util.Calendar;
import java.util.Date;

public class CalendarFragment extends Fragment implements ScheduleAdapter.OnScheduleListener {

    private ScheduleViewModel viewModel;
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TextView textViewNoSchedules;
    private ScheduleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ScheduleViewModel.class);

        // Initialize views
        calendarView = view.findViewById(R.id.calendar_view);
        recyclerView = view.findViewById(R.id.recycler_view);
        textViewNoSchedules = view.findViewById(R.id.text_view_no_schedules);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScheduleAdapter(this);
        recyclerView.setAdapter(adapter);

        // Set initial date to today
        Date today = new Date();
        viewModel.setSelectedDate(today);

        // Setup CalendarView listener
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            viewModel.setSelectedDate(calendar.getTime());
        });

        // Observe schedules for selected date
        viewModel.getSchedulesBySelectedDate().observe(getViewLifecycleOwner(), schedules -> {
            adapter.submitList(schedules);

            if (schedules == null || schedules.isEmpty()) {
                textViewNoSchedules.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textViewNoSchedules.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
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
}