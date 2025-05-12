package com.example.planwise.ui.adapter;

import android.graphics.Paint;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
            // 设置格式化后的时间描述
            holder.textViewTime.setText(getFormattedTimeDescription(currentSchedule.getScheduledDate()));
        } else {
            holder.textViewTime.setText("全天");
        }

        holder.textViewCategory.setText(currentSchedule.getCategory());
        holder.checkBoxCompleted.setChecked(currentSchedule.isCompleted());

        // 应用删除线样式如果已完成
        if (currentSchedule.isCompleted()) {
            holder.textViewTitle.setPaintFlags(holder.textViewTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textViewTitle.setPaintFlags(holder.textViewTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    /**
     * 获取格式化的时间描述
     * @param date 日期
     * @return 格式化的时间描述，例如 "今天 14:30" 或 "还有2天"
     */
    private String getFormattedTimeDescription(Date date) {
        if (date == null) return "";

        Calendar scheduleCalendar = Calendar.getInstance();
        scheduleCalendar.setTime(date);

        Calendar nowCalendar = Calendar.getInstance();

        // 计算日期差异
        long diffInMillis = scheduleCalendar.getTimeInMillis() - nowCalendar.getTimeInMillis();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

        // 检查是否是同一天（今天）
        boolean isSameDay = scheduleCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR) &&
                scheduleCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR);

        // 检查是否是明天
        Calendar tomorrowCalendar = Calendar.getInstance();
        tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);
        boolean isTomorrow = scheduleCalendar.get(Calendar.YEAR) == tomorrowCalendar.get(Calendar.YEAR) &&
                scheduleCalendar.get(Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(Calendar.DAY_OF_YEAR);

        // 格式化时间
        String timeStr = timeFormat.format(date);

        if (isSameDay) {
            return "今天 " + timeStr;
        } else if (isTomorrow) {
            return "明天 " + timeStr;
        } else if (diffInDays > 0 && diffInDays < 7) {
            // 未来一周内的日期
            return "还有" + diffInDays + "天";
        } else if (diffInDays < 0) {
            // 过去的日期
            return Math.abs(diffInDays) + "天前";
        } else {
            // 更远的未来日期，使用完整日期格式
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
            return dateFormat.format(date);
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