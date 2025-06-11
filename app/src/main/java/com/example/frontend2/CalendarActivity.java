package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.CompleteRoutineRequest;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarActivity extends AppCompatActivity {

    private static final String PREFS_NAME   = "UserPrefs";
    private static final String KEY_USER_ID  = "user_id";

    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private LinearLayout doneContainer, todoContainer;
    private CleaningRoutineApi routineApi;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // 뷰 바인딩
        calendarView   = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        doneContainer  = findViewById(R.id.doneContainer);
        todoContainer  = findViewById(R.id.todoContainer);

        // 로그인된 userId 로딩
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, -1);
        if (currentUserId == -1) {
            // 로그인 정보 없으면 로그인 화면으로
            startActivity(new Intent(this, Login_UI.class));
            finish();
            return;
        }

        // Retrofit API 초기화
        routineApi = ApiClient.getClient().create(CleaningRoutineApi.class);

        // 날짜 선택 리스너
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            String display = String.format(Locale.getDefault(), "%d년 %d월 %d일", year, month + 1, dayOfMonth);
            tvSelectedDate.setText(display);
            loadRoutinesForDate(dateStr);
        });

        // 오늘 날짜로 초기 로드
        calendarView.setDate(System.currentTimeMillis());
    }

    private void loadRoutinesForDate(String date) {
        // 컨테이너 초기화
        doneContainer.removeAllViews();
        todoContainer.removeAllViews();

        // API 호출
        routineApi.getRoutinesByDate(currentUserId, date)
                .enqueue(new Callback<List<CleaningRoutine>>() {
                    @Override
                    public void onResponse(Call<List<CleaningRoutine>> call, Response<List<CleaningRoutine>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (CleaningRoutine r : response.body()) {
                                addTaskView(r, todoContainer, false);
                            }
                        } else {
                            Toast.makeText(CalendarActivity.this, "루틴을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CleaningRoutine>> call, Throwable t) {
                        Toast.makeText(CalendarActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addTaskView(CleaningRoutine r, LinearLayout container, boolean isDone) {
        View item = getLayoutInflater().inflate(R.layout.item_task, container, false);
        ImageView ivType = item.findViewById(R.id.ivTypeIcon);
        ImageView ivRoom = item.findViewById(R.id.ivRoomIcon);
        TextView tv = item.findViewById(R.id.tvContent);
        Button btnComplete = item.findViewById(R.id.btnComplete); // 버튼 참조 추가

        ivType.setImageResource(isDone ? R.drawable.ic_check : R.drawable.ic_pin);
        ivRoom.setImageResource(getIconForSpace(r.getSpaceName()));
        tv.setText(r.getSpaceName() + " > " + r.getTitle());

        // 완료 버튼 로직 추가
        btnComplete.setText("완료");
        btnComplete.setBackgroundColor(Color.parseColor("#FF6200EE"));
        btnComplete.setOnClickListener(v -> {
            boolean newState = true; // 캘린더에서는 항상 완료 처리만 지원

            CompleteRoutineRequest request = new CompleteRoutineRequest(r.getRoutine_id(), newState);

            CleaningRoutineApi api = ApiClient.getClient().create(CleaningRoutineApi.class);
            api.toggleRoutineComplete(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        btnComplete.setText("완료됨");
                        btnComplete.setEnabled(false);
                        btnComplete.setBackgroundColor(Color.LTGRAY);
                    } else {
                        Toast.makeText(CalendarActivity.this, "완료 처리 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(CalendarActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


        container.addView(item);
    }


    private int getIconForSpace(String space) {
        switch (space) {
            case "거실":     return R.drawable.ic_livingroom;
            case "침실":     return R.drawable.ic_room;
            case "화장실":   return R.drawable.ic_toilet;
            case "옷방":     return R.drawable.ic_wardrobe;
            default:          return R.drawable.ic_default;
        }
    }
}
