// CalendarActivity.java
package com.example.frontend2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvSelectedDate;
    private LinearLayout doneContainer, todoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        doneContainer = findViewById(R.id.doneContainer);
        todoContainer = findViewById(R.id.todoContainer);

        // TODO: 날짜 선택에 따라 tvSelectedDate.setText(...) 처리 필요
        tvSelectedDate.setText("2025년 5월 25일");

        // TODO: 백엔드 연동 전 테스트용 더미 데이터 (연동 시 삭제 예정)
        List<CleaningTask> doneTasks = new ArrayList<>();
        List<CleaningTask> todoTasks = new ArrayList<>();

        doneTasks.add(new CleaningTask("거실", "바닥 청소"));
        doneTasks.add(new CleaningTask("화장실", "세면대 닦기"));

        todoTasks.add(new CleaningTask("방", "이불 정리"));
        todoTasks.add(new CleaningTask("옷방", "옷 정리"));
        // TODO 끝: showCleaningTasks(...)는 실제 백엔드 데이터로 대체 필요
        showCleaningTasks(doneTasks, doneContainer, true);
        showCleaningTasks(todoTasks, todoContainer, false);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navAi = findViewById(R.id.navAi);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, Main_UI.class);
            startActivity(intent);
            finish();  // 현재 액티비티 종료 (원하면)
        });

        navCalendar.setOnClickListener(v -> {
            // 현재 페이지 → 아무 작업 안함
        });

        navAi.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, RoutineMainActivity.class);
            startActivity(intent);
            finish();  // 현재 액티비티 종료 (원하면)
        });



    }

    private void showCleaningTasks(List<CleaningTask> tasks, LinearLayout container, boolean isDone) {
        container.removeAllViews();

        for (CleaningTask task : tasks) {
            View itemView = getLayoutInflater().inflate(R.layout.item_task, container, false);

            ImageView ivTypeIcon = itemView.findViewById(R.id.ivTypeIcon);
            ImageView ivRoomIcon = itemView.findViewById(R.id.ivRoomIcon);
            TextView tvContent = itemView.findViewById(R.id.tvContent);

            // 완료 or 예정 아이콘
            ivTypeIcon.setImageResource(isDone ? R.drawable.ic_check : R.drawable.ic_pin);

            // 공간 아이콘
            ivRoomIcon.setImageResource(getIconForSpace(task.space));

            // 내용 표시: 공간명 + 청소내용
            tvContent.setText(task.space + " > " + task.task);

            container.addView(itemView);
        }
    }

    private int getIconForSpace(String space) {
        switch (space) {
            case "거실": return R.drawable.ic_livingroom;
            case "방": return R.drawable.ic_room;
            case "화장실": return R.drawable.ic_toilet;
            case "옷방": return R.drawable.ic_wardrobe;
            default: return R.drawable.ic_default; // 기본 아이콘
        }
    }

    static class CleaningTask {
        String space;
        String task;

        public CleaningTask(String space, String task) {
            this.space = space;
            this.task = task;
        }
    }
}
