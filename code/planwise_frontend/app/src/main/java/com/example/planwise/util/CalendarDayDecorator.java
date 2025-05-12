package com.example.planwise.util;

import android.content.Context;

import com.example.planwise.data.model.Schedule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 用于跟踪日历中哪些日期有未完成任务
 */
public class CalendarDayDecorator {

    private final Context context;
    private Set<String> daysWithTasks = new HashSet<>();
    private SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public CalendarDayDecorator(Context context) {
        this.context = context;
    }

    /**
     * 从日程列表中获取有未完成任务的天数
     * @param schedules 日程列表
     */
    public void setupTaskDays(List<Schedule> schedules) {
        daysWithTasks.clear();
        if (schedules == null) return;

        for (Schedule schedule : schedules) {
            if (!schedule.isCompleted() && schedule.getScheduledDate() != null) {
                // 使用yyyy-MM-dd格式的字符串作为日期标识符
                Calendar cal = Calendar.getInstance();
                cal.setTime(schedule.getScheduledDate());

                // 清除时分秒，只保留年月日
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                String dateKey = dateKeyFormat.format(cal.getTime());
                daysWithTasks.add(dateKey);
            }
        }
    }

    /**
     * 检查指定日期是否有未完成的任务
     * @param year 年
     * @param month 月（0-11）
     * @param dayOfMonth 日
     * @return 是否有未完成任务
     */
    public boolean hasTasksOnDay(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        String dateKey = dateKeyFormat.format(calendar.getTime());
        return daysWithTasks.contains(dateKey);
    }

    /**
     * 获取有任务的日期列表
     * @return 有任务的日期集合
     */
    public Set<String> getDaysWithTasks() {
        return daysWithTasks;
    }
}