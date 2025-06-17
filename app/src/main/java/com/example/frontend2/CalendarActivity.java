package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningLogApi;
import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.models.CleaningRoutine;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "CleanItAppPrefs";
    private static final String KEY_USER_ID = "logged_in_user_id";

    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private LinearLayout doneContainer, todoContainer;
    private CleaningRoutineApi routineApi;
    private CleaningLogApi routineLogApi;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        doneContainer = findViewById(R.id.doneContainer);
        todoContainer = findViewById(R.id.todoContainer);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, -1);
        if (currentUserId == -1) {
            startActivity(new Intent(this, Login_UI.class));
            finish();
            return;
        }

        routineApi = ApiClient.getClient().create(CleaningRoutineApi.class);
        routineLogApi = ApiClient.getClient().create(CleaningLogApi.class);

        // 날짜 선택 리스너
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvSelectedDate.setText(String.format("%d년 %d월 %d일", year, month + 1, dayOfMonth));
            loadRoutinesForDate(dateStr);
        });

        // 앱 시작시 오늘 날짜로 기본 조회
        calendarView.setDate(System.currentTimeMillis());
        String todayDateStr = getTodayDateString();
        tvSelectedDate.setText(todayDateStr.replace("-", "년 ").replaceFirst("-", "월 ") + "일");
        loadRoutinesForDate(todayDateStr);

        setupNavigation();
    }

    private void setupNavigation() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navAi = findViewById(R.id.navAi);

        navHome.setOnClickListener(v -> navigateTo(Main_UI.class, true));
        navCalendar.setOnClickListener(v -> {});
        navAi.setOnClickListener(v -> navigateTo(RoutineMainActivity.class, true));
    }

    private void navigateTo(Class<?> cls, boolean finishCurrent) {
        startActivity(new Intent(this, cls));
        if (finishCurrent) finish();
    }

    private void loadRoutinesForDate(String date) {
        doneContainer.removeAllViews();
        todoContainer.removeAllViews();

        loadTodoRoutines(date);
        loadDoneRoutines(date);
    }

    private void loadTodoRoutines(String date) {
        routineApi.getRoutinesByDate(currentUserId, date)
                .enqueue(new Callback<List<CleaningRoutine>>() {
                    @Override
                    public void onResponse(Call<List<CleaningRoutine>> call, Response<List<CleaningRoutine>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (CleaningRoutine r : response.body()) {
                                addTaskView(r, todoContainer);
                            }
                        } else {
                            Toast.makeText(CalendarActivity.this, "할 일 불러오기 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<CleaningRoutine>> call, Throwable t) {
                        Toast.makeText(CalendarActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadDoneRoutines(String date) {
        routineLogApi.getCompletedRoutinesByDate(currentUserId, date)
                .enqueue(new Callback<List<CleaningRoutine>>() {
                    @Override
                    public void onResponse(Call<List<CleaningRoutine>> call, Response<List<CleaningRoutine>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (CleaningRoutine r : response.body()) {
                                addTaskView(r, doneContainer);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<List<CleaningRoutine>> call, Throwable t) {
                        Toast.makeText(CalendarActivity.this, "완료된 일 불러오기 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addTaskView(CleaningRoutine r, LinearLayout container) {
        View item = getLayoutInflater().inflate(R.layout.item_task, container, false);
        TextView tv = item.findViewById(R.id.tvContent);
        tv.setText(r.getSpaceName() + " > " + r.getTitle());

        // 완료버튼 숨김
        item.findViewById(R.id.btnComplete).setVisibility(View.GONE);
        container.addView(item);
    }

    private String getTodayDateString() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int year = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH) + 1;
        int day = cal.get(java.util.Calendar.DAY_OF_MONTH);
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
    }
}
