package com.example.planwise.data.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.planwise.data.model.Schedule;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
    private static final String SCHEDULES_COLLECTION = "schedules";
    private static final String USERS_COLLECTION = "users";

    private FirebaseFirestore db;
    private static FirestoreHelper instance;

    public interface FirestoreCallback {
        void onSuccess(List<Schedule> schedules);
        void onFailure(Exception e);
    }

    private FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    // 将Schedule对象转换为Firestore可存储的Map
    private Map<String, Object> scheduleToMap(Schedule schedule) {
        Map<String, Object> scheduleMap = new HashMap<>();
        scheduleMap.put("id", schedule.getId());
        scheduleMap.put("title", schedule.getTitle());
        scheduleMap.put("description", schedule.getDescription());
        scheduleMap.put("scheduledDate", schedule.getScheduledDate());
        scheduleMap.put("location", schedule.getLocation());
        scheduleMap.put("category", schedule.getCategory());
        scheduleMap.put("isCompleted", schedule.isCompleted());
        scheduleMap.put("priority", schedule.getPriority());
        scheduleMap.put("isRecurring", schedule.isRecurring());
        scheduleMap.put("recurringPattern", schedule.getRecurringPattern());
        scheduleMap.put("lastSynced", new Date());
        return scheduleMap;
    }

    // 将Firestore文档转换为Schedule对象
    private Schedule documentToSchedule(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        long id = document.getLong("id") != null ? document.getLong("id") : 0;
        String title = document.getString("title");
        String description = document.getString("description");
        Date scheduledDate = document.getDate("scheduledDate");
        String location = document.getString("location");
        String category = document.getString("category");
        boolean isCompleted = document.getBoolean("isCompleted") != null ?
                document.getBoolean("isCompleted") : false;

        Schedule schedule = new Schedule(title, description, scheduledDate, location, category, isCompleted);
        schedule.setId(id);

        // 设置其他字段
        if (document.getLong("priority") != null) {
            schedule.setPriority(document.getLong("priority").intValue());
        }

        if (document.getBoolean("isRecurring") != null) {
            schedule.setRecurring(document.getBoolean("isRecurring"));
        }

        schedule.setRecurringPattern(document.getString("recurringPattern"));
        schedule.setLastSynced(document.getDate("lastSynced"));

        return schedule;
    }

    // 保存用户信息到Firestore
    public void saveUserData(String userId, String username, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("lastSync", new Date());

        db.collection(USERS_COLLECTION).document(userId)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User data saved successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error saving user data", e);
                    }
                });
    }

    // 保存单个Schedule到Firestore
    public void saveSchedule(String userId, Schedule schedule) {
        // 使用Schedule的ID作为文档ID
        String documentId = userId + "_" + schedule.getId();
        Map<String, Object> scheduleMap = scheduleToMap(schedule);
        scheduleMap.put("userId", userId); // 添加用户ID以便查询

        db.collection(SCHEDULES_COLLECTION).document(documentId)
                .set(scheduleMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Schedule saved successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error saving schedule", e);
                    }
                });
    }

    // 批量保存Schedule列表到Firestore
    public void saveSchedules(final String userId, List<Schedule> schedules) {
        for (Schedule schedule : schedules) {
            saveSchedule(userId, schedule);
        }
    }

    // 从Firestore获取用户的所有Schedule
    public void getSchedulesForUser(String userId, final FirestoreCallback callback) {
        db.collection(SCHEDULES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Schedule> schedules = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Schedule schedule = documentToSchedule(document);
                                if (schedule != null) {
                                    schedules.add(schedule);
                                }
                            }
                            callback.onSuccess(schedules);
                        } else {
                            Log.e(TAG, "Error getting schedules", task.getException());
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    // 删除Firestore中的Schedule
    public void deleteSchedule(String userId, long scheduleId) {
        String documentId = userId + "_" + scheduleId;
        db.collection(SCHEDULES_COLLECTION).document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Schedule deleted successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error deleting schedule", e);
                    }
                });
    }
}