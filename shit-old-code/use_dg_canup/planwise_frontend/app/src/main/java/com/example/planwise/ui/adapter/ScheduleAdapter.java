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