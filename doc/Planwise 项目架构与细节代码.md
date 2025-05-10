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
│   │   ├── dialog_time_filter.xml
│   │   ├── fragment_calendar.xml
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
└── AndroidManifest.xml
```

### JAVA（依照框架内容进行排序）

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

```java
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
```

```java
package com.example.planwise.data.repository;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.planwise.data.model.User;

public class UserRepository {
    private SharedPreferences prefs;
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();

    public UserRepository(Application application) {
        prefs = PreferenceManager.getDefaultSharedPreferences(application);
        isLoggedIn.setValue(isUserLoggedIn());
        if (isUserLoggedIn()) {
            loadUserFromPrefs();
        }
    }

    private boolean isUserLoggedIn() {
        return prefs.contains("user_id");
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

    public void login(String userId, String username, String email) {
        User user = new User(userId, username, email);

        // Save to preferences
        prefs.edit()
                .putString("user_id", userId)
                .putString("username", username)
                .putString("email", email)
                .apply();

        currentUser.setValue(user);
        isLoggedIn.setValue(true);
    }

    public void logout() {
        prefs.edit()
                .remove("user_id")
                .remove("username")
                .remove("email")
                .remove("photo_url")
                .apply();

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

```java
package com.example.planwise.ui.activity;

import android.os.Bundle;

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

//    private ScheduleViewModel scheduleViewModel;
//    private UserViewModel userViewModel;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//         Setup ViewModels
//        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
//        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Setup navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Connect BottomNavigationView with NavController
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Observe login state
//        userViewModel.getIsLoggedIn().observe(this, isLoggedIn -> {
//            // Optional: Handle login/logout state changes
//        });
    }
}
```

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

```java
package com.example.planwise.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
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
            // In a real app, this would launch a login screen
            // For simplicity, we'll just do a mock login
            userViewModel.login("user123", "Demo User", "user@example.com");
            Toast.makeText(getContext(), "Logged in as Demo User", Toast.LENGTH_SHORT).show();
        });

        buttonLogout.setOnClickListener(v -> {
            userViewModel.logout();
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
        });

        buttonSyncNow.setOnClickListener(v -> {
            User currentUser = userViewModel.getCurrentUser().getValue();
            if (currentUser != null && currentUser.isSyncEnabled()) {
                scheduleViewModel.syncWithCloud(currentUser.getUserId());
                Toast.makeText(getContext(), "Syncing data...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Sync is disabled", Toast.LENGTH_SHORT).show();
            }
        });

        switchSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userViewModel.updateSyncPreference(isChecked);
            Toast.makeText(getContext(), isChecked ? "Sync enabled" : "Sync disabled", Toast.LENGTH_SHORT).show();
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
            textViewUsername.setText("Not logged in");
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
}
```

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

    // Cloud sync
    public void syncWithCloud(String userId) {
        repository.syncWithCloud(userId);
    }

}
```

```java
package com.example.planwise.ui.viewmodel;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.planwise.data.model.User;
import com.example.planwise.data.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {
    private UserRepository repository;
    private LiveData<User> currentUser;
    private LiveData<Boolean> isLoggedIn;

    public UserViewModel(Application application) {
        super(application);
        repository = new UserRepository(application);
        currentUser = repository.getCurrentUser();
        isLoggedIn = repository.getIsLoggedIn();
    }

    public void login(String userId, String username, String email) {
        repository.login(userId, username, email);
    }

    public void logout() {
        repository.logout();
    }

    public void updateSyncPreference(boolean enabled) {
        repository.updateSyncPreference(enabled);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
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
                android:text="设置"
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
                android:text="立即同步" />
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <Button
        android:id="@+id/button_login"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="登录" />

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

