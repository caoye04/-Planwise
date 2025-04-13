package com.example.planwise.data.repository;


import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

//import com.example.planwise.data.ApiClient;
//import com.example.planwise.data.api.ApiService;
import com.example.planwise.data.db.AppDatabase;
import com.example.planwise.data.db.ScheduleDao;
import com.example.planwise.data.model.Schedule;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleRepository {
    private ScheduleDao scheduleDao;
//    private ApiService apiService; todo
    private ExecutorService executorService;
    private LiveData<List<Schedule>> allSchedules;
    private LiveData<List<Schedule>> incompleteSchedules;
    private LiveData<List<Schedule>> completedSchedules;
    private LiveData<List<String>> allCategories;

    public ScheduleRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        scheduleDao = database.scheduleDao();
//        apiService = ApiClient.getApiService(); TODO
        executorService = Executors.newFixedThreadPool(4);

        allSchedules = scheduleDao.getAllSchedules();
        incompleteSchedules = scheduleDao.getIncompleteSchedules();
        completedSchedules = scheduleDao.getCompletedSchedules();
        allCategories = scheduleDao.getAllCategories();
    }

    // Schedule operations
    public void insert(Schedule schedule) {
        executorService.execute(() -> {
            long id = scheduleDao.insertSchedule(schedule);
            schedule.setId(id);
        });
    }

    public void update(Schedule schedule) {
        executorService.execute(() -> scheduleDao.updateSchedule(schedule));
    }

    public void delete(Schedule schedule) {
        executorService.execute(() -> scheduleDao.deleteSchedule(schedule));
    }

    public void toggleCompleted(Schedule schedule) {
        schedule.setCompleted(!schedule.isCompleted());
        update(schedule);
    }

    // Getters for LiveData
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
        return scheduleDao.getScheduleById(id);
    }

    public LiveData<List<Schedule>> getSchedulesByDate(Date date) {
        return scheduleDao.getSchedulesByDate(date);
    }

    public LiveData<List<Schedule>> getIncompleteSchedulesByCategory(String category) {
        return scheduleDao.getIncompleteSchedulesByCategory(category);
    }

    public LiveData<List<Schedule>> getSchedulesBetweenDates(Date startDate, Date endDate) {
        return scheduleDao.getSchedulesBetweenDates(startDate, endDate);
    }

    public LiveData<List<String>> getAllCategories() {
        return allCategories;
    }

    // Cloud sync operations
    public void syncWithCloud(String userId) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Step 1: Fetch local unsynced schedules
                    Date lastSyncTime = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // Last 24 hours
                    List<Schedule> unSyncedSchedules = scheduleDao.getUnSyncedSchedules(lastSyncTime);

                    // Step 2: Upload unsynced schedules to server
                    // apiService.uploadSchedules(userId, unSyncedSchedules);

                    // Step 3: Fetch latest schedules from server
                    // List<Schedule> cloudSchedules = apiService.getSchedules(userId).execute().body();

                    // Step 4: Update local database with cloud data
                    // handleCloudSchedules(cloudSchedules);

                    // Mark all as synced
                    for (Schedule schedule : unSyncedSchedules) {
                        schedule.setLastSynced(new Date());
                        scheduleDao.updateSchedule(schedule);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle sync errors
                }
            }
        });
    }

    // This would handle merging remote and local data
    private void handleCloudSchedules(List<Schedule> cloudSchedules) {
        // Implementation would merge data based on timestamps, priorities, etc.
    }
}