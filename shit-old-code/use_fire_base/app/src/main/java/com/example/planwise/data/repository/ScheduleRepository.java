package com.example.planwise.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.planwise.data.db.AppDatabase;
import com.example.planwise.data.db.ScheduleDao;
import com.example.planwise.data.firebase.FirestoreHelper;
import com.example.planwise.data.model.Schedule;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleRepository {
    private static final String TAG = "ScheduleRepository";
    private ScheduleDao scheduleDao;
    private FirestoreHelper firestoreHelper;
    private ExecutorService executorService;
    private LiveData<List<Schedule>> allSchedules;
    private LiveData<List<Schedule>> incompleteSchedules;
    private LiveData<List<Schedule>> completedSchedules;
    private LiveData<List<String>> allCategories;

    public ScheduleRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        scheduleDao = database.scheduleDao();
        firestoreHelper = FirestoreHelper.getInstance();
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

    // 将本地数据上传至云端
    public void uploadToCloud(String userId) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "开始上传本地数据到云端");
                    // 获取所有本地日程
                    List<Schedule> localSchedules = scheduleDao.getAllSchedules().getValue();
                    if (localSchedules != null && !localSchedules.isEmpty()) {
                        // 上传到Firestore
                        firestoreHelper.saveSchedules(userId, localSchedules);

                        // 更新每个日程的同步时间
                        Date syncTime = new Date();
                        for (Schedule schedule : localSchedules) {
                            schedule.setLastSynced(syncTime);
                            scheduleDao.updateSchedule(schedule);
                        }
                        Log.d(TAG, "成功上传了 " + localSchedules.size() + " 条日程");
                    } else {
                        Log.d(TAG, "没有本地数据需要上传");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "上传数据到云端时出错", e);
                }
            }
        });
    }

    // 从云端下载数据到本地
    public void downloadFromCloud(String userId) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "开始从云端下载数据");
                firestoreHelper.getSchedulesForUser(userId, new FirestoreHelper.FirestoreCallback() {
                    @Override
                    public void onSuccess(List<Schedule> cloudSchedules) {
                        if (cloudSchedules != null && !cloudSchedules.isEmpty()) {
                            Log.d(TAG, "从云端获取了 " + cloudSchedules.size() + " 条日程");
                            // 将云端数据保存到本地数据库
                            for (Schedule cloudSchedule : cloudSchedules) {
                                // 设置同步时间
                                cloudSchedule.setLastSynced(new Date());

                                // 插入或更新本地数据库
                                try {
                                    Schedule localSchedule = scheduleDao.getScheduleById(cloudSchedule.getId()).getValue();
                                    if (localSchedule != null) {
                                        // 如果本地已存在，则更新
                                        scheduleDao.updateSchedule(cloudSchedule);
                                    } else {
                                        // 如果本地不存在，则插入
                                        scheduleDao.insertSchedule(cloudSchedule);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "保存云端日程到本地时出错", e);
                                }
                            }
                            Log.d(TAG, "云端数据同步到本地完成");
                        } else {
                            Log.d(TAG, "云端没有数据需要下载");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "从云端下载数据时出错", e);
                    }
                });
            }
        });
    }

    // 双向同步
    public void syncWithCloud(String userId) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "开始双向同步");
                    // 步骤1：获取所有未同步的本地数据
                    Date lastSyncTime = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // 最近24小时
                    List<Schedule> unSyncedSchedules = scheduleDao.getUnSyncedSchedules(lastSyncTime);

                    // 步骤2：上传未同步的数据到云端
                    if (unSyncedSchedules != null && !unSyncedSchedules.isEmpty()) {
                        Log.d(TAG, "上传 " + unSyncedSchedules.size() + " 条未同步的日程到云端");
                        firestoreHelper.saveSchedules(userId, unSyncedSchedules);

                        // 更新同步时间
                        Date syncTime = new Date();
                        for (Schedule schedule : unSyncedSchedules) {
                            schedule.setLastSynced(syncTime);
                            scheduleDao.updateSchedule(schedule);
                        }
                    }

                    // 步骤3：从云端下载数据
                    downloadFromCloud(userId);

                } catch (Exception e) {
                    Log.e(TAG, "双向同步时出错", e);
                }
            }
        });
    }
}