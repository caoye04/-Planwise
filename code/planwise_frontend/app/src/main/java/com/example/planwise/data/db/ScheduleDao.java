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

    @Query("SELECT * FROM schedules ORDER BY scheduledDate ASC")
    List<Schedule> getAllSchedulesSync();

    @Query("DELETE FROM schedules")
    void deleteAllSchedules();

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