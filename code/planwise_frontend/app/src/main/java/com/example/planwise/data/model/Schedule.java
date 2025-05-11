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