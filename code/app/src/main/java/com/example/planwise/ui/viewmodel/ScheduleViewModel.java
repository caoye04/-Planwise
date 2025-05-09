package com.example.planwise.ui.viewmodel;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.planwise.data.model.Schedule;
import com.example.planwise.data.repository.ScheduleRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ScheduleViewModel extends AndroidViewModel {
    private ScheduleRepository repository;
    private LiveData<List<Schedule>> allSchedules;
    private LiveData<List<Schedule>> incompleteSchedules;
    private LiveData<List<Schedule>> completedSchedules;
    private LiveData<List<String>> allCategories;

    // For calendar view
    private MutableLiveData<Date> selectedDate = new MutableLiveData<>();
    private LiveData<List<Schedule>> schedulesBySelectedDate;

    // For category filtering
    private MutableLiveData<String> selectedCategory = new MutableLiveData<>();
    private LiveData<List<Schedule>> schedulesBySelectedCategory;

    // For date range filtering
    private MutableLiveData<Date> startDate = new MutableLiveData<>();
    private MutableLiveData<Date> endDate = new MutableLiveData<>();
    private LiveData<List<Schedule>> schedulesByDateRange;

    public ScheduleViewModel(Application application) {
        super(application);
        repository = new ScheduleRepository(application);
        allSchedules = repository.getAllSchedules();
        incompleteSchedules = repository.getIncompleteSchedules();
        completedSchedules = repository.getCompletedSchedules();
        allCategories = repository.getAllCategories();

        // Initialize with today's date
        selectedDate.setValue(new Date());

        // Setup transformations for filtered data
        schedulesBySelectedDate = Transformations.switchMap(selectedDate,
                date -> repository.getSchedulesByDate(date));

        schedulesBySelectedCategory = Transformations.switchMap(selectedCategory,
                category -> repository.getIncompleteSchedulesByCategory(category));

        schedulesByDateRange = Transformations.switchMap(startDate, start ->
                Transformations.switchMap(endDate, end -> {
                    if (start != null && end != null) {
                        return repository.getSchedulesBetweenDates(start, end);
                    }
                    return repository.getAllSchedules();
                }));
    }

    // Schedule operations
    public void insertSchedule(Schedule schedule) {
        repository.insert(schedule);
    }

    public void updateSchedule(Schedule schedule) {
        repository.update(schedule);
    }

    public void deleteSchedule(Schedule schedule) {
        repository.delete(schedule);
    }

    public void toggleScheduleCompleted(Schedule schedule) {
        repository.toggleCompleted(schedule);
    }

    // LiveData getters
    public LiveData<List<Schedule>> getAllSchedules() {
        return allSchedules;
    }

    public LiveData<List<Schedule>> getIncompleteSchedules() {
        return incompleteSchedules;
    }

    public LiveData<List<Schedule>> getCompletedSchedules() {
        return completedSchedules;
    }

    public LiveData<Schedule> getScheduleById(long id) {
        return repository.getScheduleById(id);
    }

    public LiveData<List<String>> getAllCategories() {
        return allCategories;
    }

    // Date selection
    public void setSelectedDate(Date date) {
        selectedDate.setValue(date);
    }

    public LiveData<Date> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<List<Schedule>> getSchedulesBySelectedDate() {
        return schedulesBySelectedDate;
    }

    // Category selection
    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
    }

    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    public LiveData<List<Schedule>> getSchedulesBySelectedCategory() {
        return schedulesBySelectedCategory;
    }

    // Date range selection
    public void setDateRange(Date start, Date end) {
        startDate.setValue(start);
        endDate.setValue(end);
    }

    public LiveData<List<Schedule>> getSchedulesByDateRange() {
        return schedulesByDateRange;
    }

    // Today's schedules
    public LiveData<List<Schedule>> getTodaySchedules() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startOfDay = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endOfDay = calendar.getTime();

        return repository.getSchedulesBetweenDates(startOfDay, endOfDay);
    }

    // Cloud sync
    public void syncWithCloud(String userId) {
        repository.syncWithCloud(userId);
    }

}