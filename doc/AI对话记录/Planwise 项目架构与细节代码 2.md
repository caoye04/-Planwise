你好！我最近在写一个TODOLISTAPP的项目。它的要求与项目框架如下。具体代码细节可以看附件中的md文件。


我目前已经完成了：添加、排序、筛选等基础功能

也尝试做了依靠firebase做的登录与数据云同步功能。

但是在现在的情况下是我登陆后是不显示登录状态的，应该就是登陆失败了我很不理解！

帮我分析一下目前的代码应该怎么改





# Planwise 项目架构与细节代码

### 项目功能点需求

添加待办事项，为待办事项添加分类标签、时间、地点。

通过点击快速修改完成/未完成的待办事项状态。

按时间排序待办事项。

按类别筛选未完成的待办事项。

筛选某一时间段的待办事项。

查看全部已完成/未完成的待办事项。

将本地数据与云端同步。



### 项目框架

```cmd
app/
├── java/
│	├── data/
│	│   ├── db/
│	│   │   ├── AppDatabase.java
│	│   │   ├── DateConverter.java
│	│   │   └── ScheduleDao.java
│	│   ├── firebase/
│	│   │   ├── FirebaseAuthHelper.java
│	│   │   ├── FirestoreHelper.java
│	│   ├── model/
│	│   │   ├── Schedule.java
│	│   │   └── User.java
│	│   └── repository/
│	│       ├── ScheduleRepository.java
│	│       └── UserRepository.java
│	└── ui/
│		├── activity/
│		│   ├── AddScheduleActivity.java
│		│   ├── MainActivity.java
│		│   └── ScheduleDetailActivity.java
│		├── adapter/
│		│   └── ScheduleAdapter.java
│		├── fragment/
│		│   ├── CalendarFragment.java
│		│   ├── ProfileFragment.java
│		│   └── TodayTodoFragment.java
│		└── viewmodel/
│			├── ScheduleViewModel.java
│			└── UserViewModel.java
├── res/
│   ├── color/
│   ├── drawable/
│   ├── layout/
│   │   ├── activity_add_schedule.xml
│   │   ├── activity_main.xml
│   │   ├── activity_schedule_detail.xml
│   │   ├── dialog_login.xml
│   │   ├── dialog_sync_options.xml
│   │   ├── dialog_time_filter.xml
│   │   ├── fragment_calendar.xml
│   │   ├── fragment_profile.xml
│   │   ├── fragment_profile.xml
│   │   ├── fragment_today_todo.xml
│   │   └── item_schedule.xml
│   ├── menu/
│   │   ├── bottom_nav_menu.xml
│   │   └── menu_schedule_detail.xml
│   ├── mipmap
│   ├── navigation/
│   ├── values/
│   └── xml/
├── google-services.json
├── build.gradle.kts(Project)
├── build.gradle.kts(APP)
├── settings.gradle.kts
└── AndroidManifest.xml
```

### google-services.json

```
{
  "project_info": {
    "project_number": "616297993239",
    "project_id": "planwise-38e69",
    "storage_bucket": "planwise-38e69.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:616297993239:android:e8ab5f068b072aad077852",
        "android_client_info": {
          "package_name": "com.example.planwise"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "AIzaSyCLI-qfAMK7iD2Tgf3TOiiF6fEyt7Z5MoM"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
```

### build.gradle.kts(Project)

```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}
buildscript {
    dependencies {
        // 添加Google Services Gradle插件
        classpath("com.google.gms:google-services:4.4.1")
    }
}
```

### build.gradle.kts(APP)

```kts
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.planwise"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.planwise"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Room数据库依赖
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Lifecycle组件 (ViewModel和LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    // WorkManager (用于后台同步)
    implementation("androidx.work:work-runtime:2.9.0")

    // Retrofit (网络请求)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Gson (JSON解析)
    implementation("com.google.code.gson:gson:2.10.1")

    // Firebase依赖
    implementation(platform("com.google.firebase:firebase-bom:32.2.3"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
}
```

### AndroidManifest.xml

```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- 添加必要的权限 -->
    <uses-permission android:name="android.permission.INTERNET" /> <!-- 云同步需要 -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> <!-- 检查网络状态 -->
    <uses-permission android:name="android.permission.VIBRATE" /> <!-- 通知振动 -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" /> <!-- 设备重启后恢复提醒 -->

    <!--        android:name=".PlanwiseApplication"-->
    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApp.NoActionBar"
        tools:targetApi="31">

        <!-- 主Activity -->
        <activity
            android:name=".ui.activity.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- 添加日程Activity -->
        <activity
            android:name=".ui.activity.AddScheduleActivity"
            android:exported="false"
            android:theme="@style/Theme.MyApp.ActionBar"
            android:parentActivityName=".ui.activity.MainActivity" />

        <!-- 日程详情Activity -->
        <activity
            android:name=".ui.activity.ScheduleDetailActivity"
            android:exported="false"
            android:theme="@style/Theme.MyApp.ActionBar"
            android:parentActivityName=".ui.activity.MainActivity" />

        <!-- 通知服务 -->
<!--        <receiver-->
<!--            android:name=".utils.NotificationReceiver"-->
<!--            android:exported="false">-->
<!--            <intent-filter>-->
<!--                <action android:name="android.intent.action.BOOT_COMPLETED" />-->
<!--            </intent-filter>-->
<!--        </receiver>-->

        <!-- 确保WorkManager正常工作的Provider -->
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup"
                tools:node="remove" />
        </provider>

    </application>

</manifest>
```

### JAVA（依照框架内容进行排序）

#### db / AppDatabase.java

```java
package com.example.planwise.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.planwise.data.db.DateConverter;
import com.example.planwise.data.db.ScheduleDao;
import com.example.planwise.data.model.Schedule;

@Database(entities = {Schedule.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "planwise_db";
    private static AppDatabase instance;

    public abstract ScheduleDao scheduleDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
```

#### db / DateConverter.java

```java
package com.example.planwise.data.db;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}

```

#### db /  ScheduleDao.java

```java
package com.example.planwise.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.planwise.data.model.Schedule;

import java.util.Date;
import java.util.List;

@Dao
public interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSchedule(Schedule schedule);

    @Update
    void updateSchedule(Schedule schedule);

    @Delete
    void deleteSchedule(Schedule schedule);

    @Query("SELECT * FROM schedules WHERE id = :id")
    LiveData<Schedule> getScheduleById(long id);

    @Query("SELECT * FROM schedules ORDER BY scheduledDate ASC")
    LiveData<List<Schedule>> getAllSchedules();

    @Query("SELECT * FROM schedules WHERE isCompleted = 0 ORDER BY scheduledDate ASC")
    LiveData<List<Schedule>> getIncompleteSchedules();

    @Query("SELECT * FROM schedules WHERE isCompleted = 1 ORDER BY scheduledDate DESC")
    LiveData<List<Schedule>> getCompletedSchedules();

    @Query("SELECT * FROM schedules WHERE date(scheduledDate/1000, 'unixepoch') = date(:date/1000, 'unixepoch') ORDER BY scheduledDate ASC")
    LiveData<List<Schedule>> getSchedulesByDate(Date date);

    @Query("SELECT * FROM schedules WHERE category = :category AND isCompleted = 0 ORDER BY scheduledDate ASC")
    LiveData<List<Schedule>> getIncompleteSchedulesByCategory(String category);

    @Query("SELECT * FROM schedules WHERE scheduledDate BETWEEN :startDate AND :endDate ORDER BY scheduledDate ASC")
    LiveData<List<Schedule>> getSchedulesBetweenDates(Date startDate, Date endDate);

    @Query("SELECT DISTINCT category FROM schedules")
    LiveData<List<String>> getAllCategories();

    @Query("SELECT * FROM schedules WHERE lastSynced IS NULL OR lastSynced < :lastSync")
    List<Schedule> getUnSyncedSchedules(Date lastSync);
}
```

#### firebase/FirebaseAuthHelper.java

```java
package com.example.planwise.data.firebase;

import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class FirebaseAuthHelper {
    private static final String TAG = "FirebaseAuthHelper";
    private FirebaseAuth mAuth;
    private static FirebaseAuthHelper instance;
    private AuthCallback mCallback;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    private FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseAuthHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthHelper();
        }
        return instance;
    }

    public void setAuthCallback(AuthCallback callback) {
        this.mCallback = callback;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            if (mCallback != null) {
                                mCallback.onSuccess(mAuth.getCurrentUser());
                            }
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            if (mCallback != null) {
                                mCallback.onFailure(task.getException());
                            }
                        }
                    }
                });
    }

    public void createUserWithEmailAndPassword(String email, String password, final String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                                if (mCallback != null) {
                                                    mCallback.onSuccess(user);
                                                }
                                            }
                                        }
                                    });
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            if (mCallback != null) {
                                mCallback.onFailure(task.getException());
                            }
                        }
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
    }
}
```

#### firebase/FirestoreHelper.java

```java
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
```

#### model / Schedule.java

```java
package com.example.planwise.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.planwise.data.db.DateConverter;

import java.util.Date;

@Entity(tableName = "schedules")
public class Schedule {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String description;

    @TypeConverters(DateConverter.class)
    private Date scheduledDate;

    private String location;
    private String category;
    private boolean isCompleted;
    private int priority; // 1-3 representing low, medium, high
    private boolean isRecurring;
    private String recurringPattern; // daily, weekly, monthly

    // Last sync timestamp for cloud sync
    @TypeConverters(DateConverter.class)
    private Date lastSynced;

    // Constructor, getters and setters
    public Schedule(String title, String description, Date scheduledDate,
                    String location, String category, boolean isCompleted) {
        this.title = title;
        this.description = description;
        this.scheduledDate = scheduledDate;
        this.location = location;
        this.category = category;
        this.isCompleted = isCompleted;
        this.priority = 2; // Default medium priority
        this.isRecurring = false;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getRecurringPattern() {
        return recurringPattern;
    }

    public void setRecurringPattern(String recurringPattern) {
        this.recurringPattern = recurringPattern;
    }

    public Date getLastSynced() {
        return lastSynced;
    }

    public void setLastSynced(Date lastSynced) {
        this.lastSynced = lastSynced;
    }
}
```

#### model / User.java

```java
package com.example.planwise.data.model;

public class User {
    private String userId;
    private String username;
    private String email;
    private String photoUrl;
    private boolean syncEnabled;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.syncEnabled = false;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isSyncEnabled() {
        return syncEnabled;
    }

    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }
}
```

#### repository / ScheduleRepository.java

```java
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
```

#### repository/UserRepository.java

```java
package com.example.planwise.data.repository;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.planwise.data.firebase.FirebaseAuthHelper;
import com.example.planwise.data.firebase.FirestoreHelper;
import com.example.planwise.data.model.User;
import com.google.firebase.auth.FirebaseUser;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private SharedPreferences prefs;
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();
    private FirebaseAuthHelper authHelper;
    private FirestoreHelper firestoreHelper;

    public UserRepository(Application application) {
        prefs = PreferenceManager.getDefaultSharedPreferences(application);
        authHelper = FirebaseAuthHelper.getInstance();
        firestoreHelper = FirestoreHelper.getInstance();

        // 设置Firebase认证回调
        authHelper.setAuthCallback(new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // 从Firebase用户创建应用用户
                User user = new User(
                        firebaseUser.getUid(),
                        firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                        firebaseUser.getEmail()
                );

                // 保存到SharedPreferences
                saveUserToPrefs(user);

                // 更新LiveData
                currentUser.setValue(user);
                isLoggedIn.setValue(true);

                // 保存用户数据到Firestore
                firestoreHelper.saveUserData(user.getUserId(), user.getUsername(), user.getEmail());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Authentication failed", e);
                isLoggedIn.setValue(false);
            }
        });

        // 检查是否已经登录
        FirebaseUser firebaseUser = authHelper.getCurrentUser();
        if (firebaseUser != null) {
            User user = new User(
                    firebaseUser.getUid(),
                    firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                    firebaseUser.getEmail()
            );
            currentUser.setValue(user);
            isLoggedIn.setValue(true);
        } else {
            isLoggedIn.setValue(false);
            loadUserFromPrefs(); // 尝试从本地加载
        }
    }

    private void saveUserToPrefs(User user) {
        prefs.edit()
                .putString("user_id", user.getUserId())
                .putString("username", user.getUsername())
                .putString("email", user.getEmail())
                .apply();
    }

    private void loadUserFromPrefs() {
        String userId = prefs.getString("user_id", null);
        String username = prefs.getString("username", null);
        String email = prefs.getString("email", null);
        String photoUrl = prefs.getString("photo_url", null);
        boolean syncEnabled = prefs.getBoolean("sync_enabled", false);

        if (userId != null && username != null) {
            User user = new User(userId, username, email);
            user.setPhotoUrl(photoUrl);
            user.setSyncEnabled(syncEnabled);
            currentUser.setValue(user);
        }
    }

    public void login(String email, String username) {
        // 简单起见，使用邮箱作为密码（实际应用应使用真实密码机制）
        String password = email;

        // 先尝试登录
        authHelper.signInWithEmailAndPassword(email, password);
    }

    public void registerAndLogin(String email, String username) {
        // 简单起见，使用邮箱作为密码
        String password = email;

        // 注册新用户
        authHelper.createUserWithEmailAndPassword(email, password, username);
    }

    public void logout() {
        // 登出Firebase
        authHelper.signOut();

        // 清除SharedPreferences
        prefs.edit()
                .remove("user_id")
                .remove("username")
                .remove("email")
                .remove("photo_url")
                .apply();

        // 更新LiveData
        currentUser.setValue(null);
        isLoggedIn.setValue(false);
    }

    public void updateSyncPreference(boolean enabled) {
        User user = currentUser.getValue();
        if (user != null) {
            user.setSyncEnabled(enabled);
            currentUser.setValue(user);

            prefs.edit()
                    .putBoolean("sync_enabled", enabled)
                    .apply();
        }
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }
}
```

#### activity/ AddScheduleActivity.java

```java
package com.example.planwise.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.example.planwise.R;
import com.example.planwise.data.model.Schedule;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddScheduleActivity extends AppCompatActivity {

    private ScheduleViewModel viewModel;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextDate;
    private EditText editTextTime;
    private EditText editTextLocation;
    private ChipGroup chipGroupCategory;
    private Button buttonSave;

    private Calendar selectedDateTime = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        // 安全地设置 ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("新建待办");
        }

        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        // Initialize views
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextDate = findViewById(R.id.edit_text_date);
        editTextTime = findViewById(R.id.edit_text_time);
        editTextLocation = findViewById(R.id.edit_text_location);
        chipGroupCategory = findViewById(R.id.chip_group_category);
        buttonSave = findViewById(R.id.button_save);

        // Initialize date and time
        updateDateText();
        updateTimeText();

        // Setup date picker dialog
        editTextDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(selectedDateTime.getTimeInMillis())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDateTime.setTimeInMillis(selection);
                updateDateText();
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        // Setup time picker dialog
        editTextTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(selectedDateTime.get(Calendar.HOUR_OF_DAY))
                    .setMinute(selectedDateTime.get(Calendar.MINUTE))
                    .setTitleText("Select time")
                    .build();

            timePicker.addOnPositiveButtonClickListener(v1 -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                selectedDateTime.set(Calendar.MINUTE, timePicker.getMinute());
                updateTimeText();
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });

        // Populate categories
        viewModel.getAllCategories().observe(this, categories -> {
            chipGroupCategory.removeAllViews();

            // Add default categories if empty
            if (categories == null || categories.isEmpty()) {
                addCategoryChip("Work");
                addCategoryChip("Personal");
                addCategoryChip("Study");
                addCategoryChip("Shopping");
            } else {
                for (String category : categories) {
                    addCategoryChip(category);
                }
            }
        });

        // Save button click listener
        buttonSave.setOnClickListener(v -> saveSchedule());
    }

    private void addCategoryChip(String category) {
        Chip chip = new Chip(this);
        chip.setText(category);
        chip.setCheckable(true);
        chipGroupCategory.addView(chip);
    }

    private void updateDateText() {
        editTextDate.setText(dateFormat.format(selectedDateTime.getTime()));
    }

    private void updateTimeText() {
        editTextTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void saveSchedule() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        if (title.isEmpty()) {
            editTextTitle.setError("Title cannot be empty");
            return;
        }

        // Get selected category
        String category = "Other";
        int checkedChipId = chipGroupCategory.getCheckedChipId();
        if (checkedChipId != -1) {
            Chip selectedChip = findViewById(checkedChipId);
            category = selectedChip.getText().toString();
        }

        // Create and save schedule
        Schedule schedule = new Schedule(
                title,
                description,
                selectedDateTime.getTime(),
                location,
                category,
                false
        );

        viewModel.insertSchedule(schedule);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
```

#### activity/MainActivity.java

```java
package com.example.planwise.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.planwise.R;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;
import com.example.planwise.ui.viewmodel.UserViewModel;

public class MainActivity extends AppCompatActivity {

    private ScheduleViewModel scheduleViewModel;
    private UserViewModel userViewModel;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup ViewModels
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Setup navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Connect BottomNavigationView with NavController
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 将登录结果转发给当前显示的Fragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null && navHostFragment.getChildFragmentManager().getFragments().size() > 0) {
            navHostFragment.getChildFragmentManager().getFragments().get(0)
                    .onActivityResult(requestCode, resultCode, data);
        }
    }
}
```

#### activity/ScheduleDetailActivity.java

```java
package com.example.planwise.ui.activity;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.planwise.R;
import com.example.planwise.data.model.Schedule;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ScheduleDetailActivity extends AppCompatActivity {

    private ScheduleViewModel viewModel;
    private Schedule currentSchedule;

    private TextView textViewTitle;
    private TextView textViewDescription;
    private TextView textViewDateTime;
    private TextView textViewLocation;
    private TextView textViewCategory;
    private CheckBox checkBoxCompleted;
    private TextView textViewAiSuggestion;

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_detail);

        // Enable back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Schedule Details");

        // Initialize views
        textViewTitle = findViewById(R.id.text_view_title);
        textViewDescription = findViewById(R.id.text_view_description);
        textViewDateTime = findViewById(R.id.text_view_date_time);
        textViewLocation = findViewById(R.id.text_view_location);
        textViewCategory = findViewById(R.id.text_view_category);
        checkBoxCompleted = findViewById(R.id.checkbox_completed);
        textViewAiSuggestion = findViewById(R.id.text_view_ai_suggestion);

        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        // Get schedule ID from intent
        long scheduleId = getIntent().getLongExtra("schedule_id", -1);
        if (scheduleId == -1) {
            Toast.makeText(this, "Error: Schedule not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Observe schedule details
        viewModel.getScheduleById(scheduleId).observe(this, schedule -> {
            if (schedule != null) {
                currentSchedule = schedule;
                displayScheduleDetails(schedule);
                generateAiSuggestion(schedule);
            } else {
                Toast.makeText(this, "Schedule not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Setup checkbox listener
        checkBoxCompleted.setOnClickListener(v -> {
            if (currentSchedule != null) {
                viewModel.toggleScheduleCompleted(currentSchedule);
            }
        });
    }

    private void displayScheduleDetails(Schedule schedule) {
        textViewTitle.setText(schedule.getTitle());

        // Description (show "No description" if empty)
        if (schedule.getDescription() != null && !schedule.getDescription().isEmpty()) {
            textViewDescription.setText(schedule.getDescription());
        } else {
            textViewDescription.setText("No description");
        }

        // Date and time
        if (schedule.getScheduledDate() != null) {
            textViewDateTime.setText(dateTimeFormat.format(schedule.getScheduledDate()));
        } else {
            textViewDateTime.setText("No date specified");
        }

        // Location (show "No location" if empty)
        if (schedule.getLocation() != null && !schedule.getLocation().isEmpty()) {
            textViewLocation.setText(schedule.getLocation());
        } else {
            textViewLocation.setText("No location");
        }

        textViewCategory.setText(schedule.getCategory());
        checkBoxCompleted.setChecked(schedule.isCompleted());
    }

    private void generateAiSuggestion(Schedule schedule) {
        // This would typically call an AI API or use a local model
        // For now, we'll use some simple heuristics

        String suggestion;
        String title = schedule.getTitle().toLowerCase();
        String category = schedule.getCategory();

        if (title.contains("meeting") || title.contains("appointment")) {
            suggestion = "Prepare necessary documents 15 minutes before your meeting.";
        } else if (category.equals("Study")) {
            suggestion = "Consider using the Pomodoro technique: 25 minutes of focus followed by a 5-minute break.";
        } else if (category.equals("Work")) {
            suggestion = "Try to complete this task during your peak productivity hours.";
        } else if (title.contains("exercise") || title.contains("workout")) {
            suggestion = "Stay hydrated and prepare your workout clothes in advance.";
        } else {
            suggestion = "Break this task into smaller steps for better productivity.";
        }

        textViewAiSuggestion.setText(suggestion);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit) {
            // Intent to edit schedule (not implemented in this code sample)
            Toast.makeText(this, "Edit functionality", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDeleteSchedule();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteSchedule() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Schedule")
                .setMessage("Are you sure you want to delete this schedule?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (currentSchedule != null) {
                        viewModel.deleteSchedule(currentSchedule);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
```

####  adapter/ ScheduleAdapter.java

```java
package com.example.planwise.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planwise.R;
import com.example.planwise.data.model.Schedule;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ScheduleAdapter extends ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder> {

    private OnScheduleListener listener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ScheduleAdapter(OnScheduleListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Schedule currentSchedule = getItem(position);
        holder.textViewTitle.setText(currentSchedule.getTitle());

        if (currentSchedule.getScheduledDate() != null) {
            holder.textViewTime.setText(timeFormat.format(currentSchedule.getScheduledDate()));
        } else {
            holder.textViewTime.setText("All day");
        }

        holder.textViewCategory.setText(currentSchedule.getCategory());
        holder.checkBoxCompleted.setChecked(currentSchedule.isCompleted());

        // Apply strike-through text style if completed
        if (currentSchedule.isCompleted()) {
            // Apply strike-through style
        } else {
            // Remove strike-through style
        }
    }

    public Schedule getScheduleAt(int position) {
        return getItem(position);
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewTime;
        private TextView textViewCategory;
        private CheckBox checkBoxCompleted;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewTime = itemView.findViewById(R.id.text_view_time);
            textViewCategory = itemView.findViewById(R.id.text_view_category);
            checkBoxCompleted = itemView.findViewById(R.id.checkbox_completed);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onScheduleClick(getItem(position));
                }
            });

            checkBoxCompleted.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onCompletedToggle(getItem(position));
                }
            });
        }
    }

    public interface OnScheduleListener {
        void onScheduleClick(Schedule schedule);
        void onCompletedToggle(Schedule schedule);
    }

    private static final DiffUtil.ItemCallback<Schedule> DIFF_CALLBACK = new DiffUtil.ItemCallback<Schedule>() {
        @Override
        public boolean areItemsTheSame(@NonNull Schedule oldItem, @NonNull Schedule newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Schedule oldItem, @NonNull Schedule newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.isCompleted() == newItem.isCompleted() &&
                    oldItem.getCategory().equals(newItem.getCategory());
        }
    };
}
```

####  fragment/CalendarFragment.java

```java
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
```

#### fragment/ProfileFragment.java

```java
package com.example.planwise.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.planwise.R;
import com.example.planwise.data.model.User;
import com.example.planwise.ui.viewmodel.ScheduleViewModel;
import com.example.planwise.ui.viewmodel.UserViewModel;

public class ProfileFragment extends Fragment {

    private UserViewModel userViewModel;
    private ScheduleViewModel scheduleViewModel;

    private TextView textViewUsername;
    private TextView textViewEmail;
    private Switch switchSync;
    private Button buttonLogin;
    private Button buttonLogout;
    private Button buttonSyncNow;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup ViewModels
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        scheduleViewModel = new ViewModelProvider(requireActivity()).get(ScheduleViewModel.class);

        // Initialize views
        textViewUsername = view.findViewById(R.id.text_view_username);
        textViewEmail = view.findViewById(R.id.text_view_email);
        switchSync = view.findViewById(R.id.switch_sync);
        buttonLogin = view.findViewById(R.id.button_login);
        buttonLogout = view.findViewById(R.id.button_logout);
        buttonSyncNow = view.findViewById(R.id.button_sync_now);

        // Observe user data
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), this::updateUI);
        userViewModel.getIsLoggedIn().observe(getViewLifecycleOwner(), this::updateLoginButtons);

        // Setup click listeners
        buttonLogin.setOnClickListener(v -> {
            showLoginDialog();
        });

        buttonLogout.setOnClickListener(v -> {
            userViewModel.logout();
            Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
        });

        buttonSyncNow.setOnClickListener(v -> {
            User currentUser = userViewModel.getCurrentUser().getValue();
            if (currentUser != null && currentUser.isSyncEnabled()) {
                showSyncOptionsDialog(currentUser.getUserId());
            } else {
                Toast.makeText(getContext(), "同步功能已禁用", Toast.LENGTH_SHORT).show();
            }
        });

        switchSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userViewModel.updateSyncPreference(isChecked);
            Toast.makeText(getContext(), isChecked ? "同步已启用" : "同步已禁用", Toast.LENGTH_SHORT).show();
            buttonSyncNow.setEnabled(isChecked);
        });
    }

    private void updateUI(User user) {
        if (user != null) {
            textViewUsername.setText(user.getUsername());
            textViewEmail.setText(user.getEmail());
            switchSync.setChecked(user.isSyncEnabled());
            switchSync.setEnabled(true);
            buttonSyncNow.setEnabled(user.isSyncEnabled());
        } else {
            textViewUsername.setText("未登录");
            textViewEmail.setText("");
            switchSync.setChecked(false);
            switchSync.setEnabled(false);
            buttonSyncNow.setEnabled(false);
        }
    }

    private void updateLoginButtons(Boolean isLoggedIn) {
        if (isLoggedIn) {
            buttonLogin.setVisibility(View.GONE);
            buttonLogout.setVisibility(View.VISIBLE);
        } else {
            buttonLogin.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.GONE);
        }
    }

    private void showLoginDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_login, null);

        final EditText editTextUsername = dialogView.findViewById(R.id.edit_text_username);
        final EditText editTextEmail = dialogView.findViewById(R.id.edit_text_email);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("登录/注册", (dialog, which) -> {
                    String username = editTextUsername.getText().toString().trim();
                    String email = editTextEmail.getText().toString().trim();

                    if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email)) {
                        Toast.makeText(getContext(), "用户名和邮箱不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 尝试登录，如果失败会自动注册
                    userViewModel.login(email, username);
                    Toast.makeText(getContext(), "正在登录...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showSyncOptionsDialog(String userId) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_sync_options, null);

        Button buttonUploadToCloud = dialogView.findViewById(R.id.button_upload_to_cloud);
        Button buttonDownloadFromCloud = dialogView.findViewById(R.id.button_download_from_cloud);
        Button buttonBothWaySync = dialogView.findViewById(R.id.button_both_way_sync);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        buttonUploadToCloud.setOnClickListener(v -> {
            scheduleViewModel.uploadToCloud(userId);
            Toast.makeText(getContext(), "正在上传本地数据到云端...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        buttonDownloadFromCloud.setOnClickListener(v -> {
            scheduleViewModel.downloadFromCloud(userId);
            Toast.makeText(getContext(), "正在从云端下载数据...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        buttonBothWaySync.setOnClickListener(v -> {
            scheduleViewModel.syncWithCloud(userId);
            Toast.makeText(getContext(), "正在进行双向同步...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
```

#### fragment/TodayTodoFragment.java

```java
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

```

#### viewmodel/ScheduleViewModel.java

```java
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


    // 上传本地数据到云端
    public void uploadToCloud(String userId) {
        repository.uploadToCloud(userId);
    }

    // 从云端下载数据到本地
    public void downloadFromCloud(String userId) {
        repository.downloadFromCloud(userId);
    }

    // 双向同步

    public void syncWithCloud(String userId) {
        repository.syncWithCloud(userId);
    }
}
```

#### viewmodel/UserViewModel.java

```java
package com.example.planwise.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.planwise.data.model.User;
import com.example.planwise.data.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {
    private UserRepository repository;
    private LiveData<User> currentUser;
    private LiveData<Boolean> isLoggedIn;
    private MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public UserViewModel(Application application) {
        super(application);
        repository = new UserRepository(application);
        currentUser = repository.getCurrentUser();
        isLoggedIn = repository.getIsLoggedIn();
    }

    public void login(String email, String username) {
        repository.login(email, username);
    }

    public void registerAndLogin(String email, String username) {
        repository.registerAndLogin(email, username);
    }

    public void logout() {
        repository.logout();
    }

    public void updateSyncPreference(boolean enabled) {
        repository.updateSyncPreference(enabled);
    }

    public void setSyncing(boolean syncing) {
        isSyncing.setValue(syncing);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }
}
```

### res（依照框架内容进行排序）

#### activity_add_schedule.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    tools:context=".ui.activity.AddScheduleActivity">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <com.google.android.material.textfield.TextInputLayout
            style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:hint="标题">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/edit_text_title"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="text" />
        </com.google.android.material.textfield.TextInputLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:orientation="horizontal">

            <com.google.android.material.textfield.TextInputLayout
                style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_marginEnd="8dp"
                android:layout_weight="1"
                android:hint="日期">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/edit_text_date"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:focusable="false"
                    android:inputType="none" />
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:hint="时间">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/edit_text_time"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:focusable="false"
                    android:inputType="none" />
            </com.google.android.material.textfield.TextInputLayout>
        </LinearLayout>

        <com.google.android.material.textfield.TextInputLayout
            style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:hint="地点">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/edit_text_location"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="text" />
        </com.google.android.material.textfield.TextInputLayout>

        <com.google.android.material.textfield.TextInputLayout
            style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:hint="备注">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/edit_text_description"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="top"
                android:inputType="textMultiLine"
                android:lines="3" />
        </com.google.android.material.textfield.TextInputLayout>
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="待办标签"
            android:textAppearance="@style/TextAppearance.MaterialComponents.Subtitle1" />

        <com.google.android.material.chip.ChipGroup
            android:id="@+id/chip_group_category"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            app:singleSelection="true" />

        <Button
            android:id="@+id/button_save"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="保存" />

    </LinearLayout>
</ScrollView>
```

#### activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    tools:context=".ui.activity.MainActivity">

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/nav_host_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:defaultNavHost="true"
        app:layout_constraintBottom_toTopOf="@+id/bottom_navigation"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:navGraph="@navigation/nav_graph" />

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_navigation"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:menu="@menu/bottom_nav_menu"
        app:itemRippleColor="@null"
        app:itemActiveIndicatorStyle="@null"
        app:itemTextColor="@color/bottom_nav_color"
        app:itemIconTint="@color/bottom_nav_color"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```

#### activity_schedule_detail.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    tools:context=".ui.activity.ScheduleDetailActivity">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">

        <TextView
            android:id="@+id/text_view_title"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:textAppearance="@style/TextAppearance.MaterialComponents.Headline5"
            app:layout_constraintEnd_toStartOf="@+id/checkbox_completed"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            tools:text="完成Android作业" />

        <CheckBox
            android:id="@+id/checkbox_completed"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <TextView
            android:id="@+id/text_view_category_label"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="分类:"
            android:textStyle="bold"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/text_view_title" />

        <TextView
            android:id="@+id/text_view_category"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:background="@drawable/bg_category"
            android:paddingHorizontal="8dp"
            android:paddingVertical="2dp"
            android:textColor="@color/white"
            app:layout_constraintBottom_toBottomOf="@+id/text_view_category_label"
            app:layout_constraintStart_toEndOf="@+id/text_view_category_label"
            app:layout_constraintTop_toTopOf="@+id/text_view_category_label"
            tools:text="学习" />

        <TextView
            android:id="@+id/text_view_date_time_label"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="时间:"
            android:textStyle="bold"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/text_view_category_label" />

        <TextView
            android:id="@+id/text_view_date_time"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            app:layout_constraintBottom_toBottomOf="@+id/text_view_date_time_label"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toEndOf="@+id/text_view_date_time_label"
            app:layout_constraintTop_toTopOf="@+id/text_view_date_time_label"
            tools:text="2025年4月15日 14:30" />

        <TextView
            android:id="@+id/text_view_location_label"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="地点:"
            android:textStyle="bold"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/text_view_date_time_label" />

        <TextView
            android:id="@+id/text_view_location"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            app:layout_constraintBottom_toBottomOf="@+id/text_view_location_label"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toEndOf="@+id/text_view_location_label"
            app:layout_constraintTop_toTopOf="@+id/text_view_location_label"
            tools:text="图书馆" />

        <TextView
            android:id="@+id/text_view_description_label"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="描述:"
            android:textStyle="bold"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/text_view_location_label" />

        <TextView
            android:id="@+id/text_view_description"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/text_view_description_label"
            tools:text="需要完成Android大作业的初步设计，包括UI设计和数据结构设计" />

        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            app:cardCornerRadius="8dp"
            app:cardElevation="2dp"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/text_view_description">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="AI小助手建议"
                    android:textAppearance="@style/TextAppearance.MaterialComponents.Subtitle1"
                    android:textStyle="bold" />

                <TextView
                    android:id="@+id/text_view_ai_suggestion"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    tools:text="建议提前准备相关资料，并分配足够的时间完成这项任务。可以将任务分解为UI设计、数据结构设计和文档编写三个部分，逐步完成。" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    </androidx.constraintlayout.widget.ConstraintLayout>
</ScrollView>
```

#### dialog_login.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="登录或注册"
        android:textAppearance="@style/TextAppearance.MaterialComponents.Headline6"
        android:layout_marginBottom="16dp"/>

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="8dp"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/edit_text_username"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="用户名"
            android:inputType="text"
            android:maxLines="1" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/edit_text_email"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="邮箱"
            android:inputType="textEmailAddress"
            android:maxLines="1" />
    </com.google.android.material.textfield.TextInputLayout>

</LinearLayout>
```

#### dialog_sync_options.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="同步选项"
        android:textAppearance="@style/TextAppearance.MaterialComponents.Headline6"
        android:layout_marginBottom="16dp"/>

    <Button
        android:id="@+id/button_upload_to_cloud"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="将本地数据同步到云端"
        android:layout_marginBottom="8dp"/>

    <Button
        android:id="@+id/button_download_from_cloud"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="将云端数据同步到本地"
        android:layout_marginBottom="8dp"/>

    <Button
        android:id="@+id/button_both_way_sync"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="双向同步"/>

</LinearLayout>
```

#### dialog_time_filter.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="开始日期"
        android:textSize="16sp"
        android:textColor="@android:color/black" />

    <EditText
        android:id="@+id/et_start_date"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:focusable="false"
        android:hint="点击选择起始日期"
        android:inputType="none" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:text="开始时间"
        android:textSize="16sp"
        android:textColor="@android:color/black" />

    <EditText
        android:id="@+id/et_start_time"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:focusable="false"
        android:hint="点击选择起始时间"
        android:inputType="none" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="结束日期"
        android:textSize="16sp"
        android:textColor="@android:color/black" />

    <EditText
        android:id="@+id/et_end_date"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:focusable="false"
        android:hint="点击选择结束日期"
        android:inputType="none" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:text="结束时间"
        android:textSize="16sp"
        android:textColor="@android:color/black" />

    <EditText
        android:id="@+id/et_end_time"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:focusable="false"
        android:hint="点击选择结束时间"
        android:inputType="none" />

</LinearLayout>
```

#### fragment_calendar.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".ui.fragment.CalendarFragment">
    <TextView
        android:id="@+id/text_view_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="日程"
        style="@style/PageTitle"/>

    <CalendarView
        android:id="@+id/calendar_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp"
        android:layout_marginTop="8dp" />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp"
        android:layout_marginTop="8dp"
        android:text="日程列表"
        android:textAppearance="@style/TextAppearance.MaterialComponents.Subtitle1" />

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recycler_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:clipToPadding="false"
            android:padding="8dp" />

        <TextView
            android:id="@+id/text_view_no_schedules"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="当天没有日程安排"
            android:textAlignment="center"
            android:visibility="gone" />
    </FrameLayout>
</LinearLayout>
```

#### fragment_profile.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    tools:context=".ui.fragment.ProfileFragment">
    <TextView
        android:id="@+id/text_view_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="我的"
        style="@style/PageTitle"/>

    <com.google.android.material.card.MaterialCardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="个人信息"
                android:textAppearance="@style/TextAppearance.MaterialComponents.Headline6" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:text="用户名"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/text_view_username"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                tools:text="Demo User" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:text="邮箱"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/text_view_email"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                tools:text="user@example.com" />
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <com.google.android.material.card.MaterialCardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp"
        app:cardCornerRadius="8dp"
        app:cardElevation="2dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="云同步设置"
                android:textAppearance="@style/TextAppearance.MaterialComponents.Headline6" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="启用云同步" />

                <Switch
                    android:id="@+id/switch_sync"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />
            </LinearLayout>

            <Button
                android:id="@+id/button_sync_now"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:text="选择同步方式"
                android:enabled="false"/>

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:text="登录后可使用云同步功能，将您的待办事项同步到云端或从云端恢复"
                android:textSize="12sp"
                android:textColor="@android:color/darker_gray"/>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <Button
        android:id="@+id/button_login"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="登录/注册" />

    <Button
        android:id="@+id/button_logout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="退出登录"
        android:visibility="gone" />
</LinearLayout>
```

#### fragment_today_todo.xml

```xml
<?xml version="1.0" encoding="utf-8"?>

<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.fragment.TodayTodoFragment"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
    <TextView
        android:id="@+id/text_view_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="今日待办"
        style="@style/PageTitle"/>
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginTop="8dp"
            android:layout_marginStart="16dp"
            android:layout_marginEnd="16dp">

            <!-- Existing ChipGroup -->
            <com.google.android.material.chip.ChipGroup
                android:id="@+id/chip_group_filter"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                app:singleSelection="true">

                <com.google.android.material.chip.Chip
                    android:id="@+id/chip_filter_all"
                    style="@style/Widget.MaterialComponents.Chip.Choice"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:checked="true"
                    android:text="全部" />

                <com.google.android.material.chip.Chip
                    android:id="@+id/chip_filter_incomplete"
                    style="@style/Widget.MaterialComponents.Chip.Choice"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="未完成" />

                <com.google.android.material.chip.Chip
                    android:id="@+id/chip_filter_completed"
                    style="@style/Widget.MaterialComponents.Chip.Choice"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="已完成" />
            </com.google.android.material.chip.ChipGroup>

            <!-- Add separate filter button -->
            <ImageButton
                android:id="@+id/btn_advanced_filter"
                android:layout_width="18dp"
                android:layout_height="18dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:contentDescription="高级筛选"
                android:scaleType="centerInside"
                android:src="@drawable/ic_filter"
                app:tint="?attr/colorPrimary" />
        </LinearLayout>
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipToPadding="false"
        android:padding="8dp" />

    <TextView
        android:id="@+id/text_view_empty"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="今天没有待办事项"
        android:textAlignment="center"
        android:visibility="gone" />
    </LinearLayout>
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_add"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:contentDescription="添加新任务"
        android:src="@android:drawable/ic_input_add" />
</FrameLayout>
```

#### item_schedule.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="8dp"
    android:layout_marginVertical="4dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">

        <CheckBox
            android:id="@+id/checkbox_completed"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <TextView
            android:id="@+id/text_view_title"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:layout_marginEnd="8dp"
            android:textSize="16sp"
            android:textStyle="bold"
            app:layout_constraintEnd_toStartOf="@+id/text_view_time"
            app:layout_constraintStart_toEndOf="@+id/checkbox_completed"
            app:layout_constraintTop_toTopOf="parent"
            tools:text="完成Android作业" />

        <TextView
            android:id="@+id/text_view_category"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:layout_marginTop="4dp"
            android:background="@drawable/bg_category"
            android:paddingHorizontal="8dp"
            android:paddingVertical="2dp"
            android:textColor="@color/white"
            android:textSize="12sp"
            app:layout_constraintStart_toEndOf="@+id/checkbox_completed"
            app:layout_constraintTop_toBottomOf="@+id/text_view_title"
            tools:text="学习" />

        <TextView
            android:id="@+id/text_view_time"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            tools:text="14:30" />

    </androidx.constraintlayout.widget.ConstraintLayout>
</com.google.android.material.card.MaterialCardView>
```

#### bottom_nav_menu.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/navigation_today"
        android:icon="@drawable/ic_bottom_nav_today"
        android:title="今日待办" />
    <item
        android:id="@+id/navigation_calendar"
        android:icon="@drawable/ic_bottom_nav_calendar"
        android:title="日程" />
    <item
        android:id="@+id/navigation_profile"
        android:icon="@drawable/ic_bottom_nav_profile"
        android:title="我的" />
</menu>
```

#### menu_schedule_detail.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <item
        android:id="@+id/action_edit"
        android:icon="@android:drawable/ic_menu_edit"
        android:title="编辑"
        app:showAsAction="ifRoom" />
    <item
        android:id="@+id/action_delete"
        android:icon="@android:drawable/ic_menu_delete"
        android:title="删除"
        app:showAsAction="ifRoom" />
</menu>
```

