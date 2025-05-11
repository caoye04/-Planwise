package com.example.planwise.data.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.planwise.data.api.ApiClient;
import com.example.planwise.data.api.ApiService;
import com.example.planwise.data.db.AppDatabase;
import com.example.planwise.data.db.ScheduleDao;
import com.example.planwise.data.model.Schedule;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleRepository {
    private ScheduleDao scheduleDao;
    private ApiService apiService;
    private ExecutorService executorService;
    private LiveData<List<Schedule>> allSchedules;
    private LiveData<List<Schedule>> incompleteSchedules;
    private LiveData<List<Schedule>> completedSchedules;
    private LiveData<List<String>> allCategories;

    // 状态回调接口
    public interface SyncCallback {
        void onSuccess();
        void onError(String message);
    }

    public ScheduleRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        scheduleDao = database.scheduleDao();
        apiService = ApiClient.getApiService();
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

    // 将本地数据上传到云端
    public void uploadToCloud(SyncCallback callback) {
        executorService.execute(() -> {
            try {
                // 获取所有日程
                List<Schedule> localSchedules = scheduleDao.getAllSchedulesSync();

                // 上传到服务器
                Call<List<Schedule>> call = apiService.syncSchedules(localSchedules);
                call.enqueue(new Callback<List<Schedule>>() {
                    @Override
                    public void onResponse(Call<List<Schedule>> call, Response<List<Schedule>> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("服务器返回错误: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Schedule>> call, Throwable t) {
                        callback.onError("网络错误: " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                callback.onError("同步错误: " + e.getMessage());
            }
        });
    }

    // 从云端下载数据到本地
    public void downloadFromCloud(SyncCallback callback) {
        executorService.execute(() -> {
            try {
                // 从服务器获取数据
                Call<List<Schedule>> call = apiService.getAllSchedules();
                call.enqueue(new Callback<List<Schedule>>() {
                    @Override
                    public void onResponse(Call<List<Schedule>> call, Response<List<Schedule>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // 清空本地数据库
                            scheduleDao.deleteAllSchedules();

                            // 将云端数据存入本地数据库
                            for (Schedule schedule : response.body()) {
                                // 确保不包含可能导致主键冲突的ID
                                schedule.setId(0);
                                scheduleDao.insertSchedule(schedule);
                            }

                            callback.onSuccess();
                        } else {
                            callback.onError("服务器返回错误: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Schedule>> call, Throwable t) {
                        callback.onError("网络错误: " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                callback.onError("同步错误: " + e.getMessage());
            }
        });
    }
}