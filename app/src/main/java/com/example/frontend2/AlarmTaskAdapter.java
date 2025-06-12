package com.example.frontend2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlarmTaskAdapter extends RecyclerView.Adapter<AlarmTaskAdapter.ViewHolder> {

    private List<AlarmTask> taskList;

    public AlarmTaskAdapter(List<AlarmTask> taskList) {
        this.taskList = taskList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName, tvAlarmTime;
        Switch switchAlarm;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvAlarmTime = itemView.findViewById(R.id.tvAlarmTime);
            switchAlarm = itemView.findViewById(R.id.switchAlarm);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlarmTask task = taskList.get(position);
        holder.tvTaskName.setText(task.getTaskName());
        holder.tvAlarmTime.setText(task.getAlarmTime());
        holder.switchAlarm.setChecked(task.isAlarmEnabled());

        holder.switchAlarm.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            task.setAlarmEnabled(isChecked);
            // TODO: 알람 on/off 처리 필요 (연동 후 구현)
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}