package com.example.planwise.data.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.planwise.data.api.ApiClient;
import com.example.planwise.data.api.ApiService;
import com.example.planwise.data.db.AppDatabase;
import com.example.planwise.data.db.ScheduleDao;
import com.example.planwise.data.model.Schedule;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class ScheduleRepository {
    private static final String TAG = "ScheduleRepository";
    private ScheduleDao scheduleDao;
    private ApiService apiService;
    private ExecutorService executorService;
    private LiveData<List<Schedule>> allSchedules;
    private LiveData<List<Schedule>> incompleteSchedules;
    private LiveData<List<Schedule>> completedSchedules;
    private LiveData<List<String>> allCategories;
    private MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

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

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    // 同步本地数据到云端
    public void syncLocalToCloud(OnSyncCompleteListener listener) {
        isSyncing.postValue(true);

        executorService.execute(() -> {
            try {
                // 获取本地所有日程
                List<Schedule> localSchedules = getAllSchedulesSync();

                // 上传到云端
                Call<List<Schedule>> call = apiService.uploadAllSchedules(localSchedules);
                Response<List<Schedule>> response = call.execute();

                if (response.isSuccessful()) {
                    // 同步成功
                    if (listener != null) {
                        listener.onSyncComplete(true, "本地数据已成功同步到云端");
                    }
                } else {
                    // 同步失败
                    if (listener != null) {
                        listener.onSyncComplete(false, "同步失败: " + response.message());
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "同步到云端失败", e);
                if (listener != null) {
                    listener.onSyncComplete(false, "同步失败: " + e.getMessage());
                }
            } finally {
                isSyncing.postValue(false);
            }
        });
    }

    // 同步云端数据到本地
    public void syncCloudToLocal(OnSyncCompleteListener listener) {
        isSyncing.postValue(true);

        executorService.execute(() -> {
            try {
                // 从云端获取数据
                Call<List<Schedule>> call = apiService.getAllSchedules();
                Response<List<Schedule>> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    // 清除本地数据并保存云端数据
                    deleteAllSchedulesSync();
                    List<Schedule> cloudSchedules = response.body();

                    // 将云端数据插入到本地数据库
                    for (Schedule schedule : cloudSchedules) {
                        // 确保ID不冲突（可能需要根据你的ID生成策略调整）
                        schedule.setId(0); // Room会自动生成新的ID
                        scheduleDao.insertSchedule(schedule);
                    }

                    if (listener != null) {
                        listener.onSyncComplete(true, "云端数据已成功同步到本地");
                    }
                } else {
                    if (listener != null) {
                        listener.onSyncComplete(false, "从云端获取数据失败: " + response.message());
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "从云端同步失败", e);
                if (listener != null) {
                    listener.onSyncComplete(false, "同步失败: " + e.getMessage());
                }
            } finally {
                isSyncing.postValue(false);
            }
        });
    }

    // 获取所有日程（同步方法）
    private List<Schedule> getAllSchedulesSync() {
        // 这个方法需要内部使用，直接同步获取所有日程
        AppDatabase db = AppDatabase.getInstance(null);
        return db.scheduleDao().getAllSchedulesSync();
    }

    // 删除所有日程（同步方法）
    private void deleteAllSchedulesSync() {
        AppDatabase db = AppDatabase.getInstance(null);
        db.scheduleDao().deleteAllSchedules();
    }

    // 同步完成监听器接口
    public interface OnSyncCompleteListener {
        void onSyncComplete(boolean success, String message);
    }
}