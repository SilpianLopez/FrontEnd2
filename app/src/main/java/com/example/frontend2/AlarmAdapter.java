package com.example.frontend2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {
    private Context context;
    private List<AlarmRoom> roomList;

    public AlarmAdapter(Context context, List<AlarmRoom> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName;
        ImageView btnToggle;
        LinearLayout taskListContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            btnToggle = itemView.findViewById(R.id.btnToggle);
            taskListContainer = itemView.findViewById(R.id.taskListContainer);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alarm_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlarmRoom room = roomList.get(position);
        holder.tvRoomName.setText(room.getRoomName());

        holder.taskListContainer.removeAllViews();
        for (AlarmTask task : room.getTaskList()) {
            View taskView = LayoutInflater.from(context).inflate(R.layout.item_alarm_task, holder.taskListContainer, false);
            TextView tvTaskName = taskView.findViewById(R.id.tvTaskName);
            TextView tvAlarmTime = taskView.findViewById(R.id.tvAlarmTime);
            tvTaskName.setText(task.getTaskName());
            tvAlarmTime.setText(task.getAlarmTime());
            holder.taskListContainer.addView(taskView);
        }

        holder.btnToggle.setOnClickListener(v -> {
            if (holder.taskListContainer.getVisibility() == View.GONE) {
                holder.taskListContainer.setVisibility(View.VISIBLE);
            } else {
                holder.taskListContainer.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }
}
