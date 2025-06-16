package com.example.frontend2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.CleaningRoutineApi;
import com.example.frontend2.api.SpaceApi;
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.CompleteRoutineRequest;
import com.example.frontend2.models.Space;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import android.app.AlarmManager;
import android.provider.Settings;


public class Main_UI extends AppCompatActivity {

    private static final String TAG = "Main_UI";
    private GridLayout spaceGrid;
    private LinearLayout todoListLayout;
    private SpaceApi spaceApiService;
    private int currentUserId = -1;

    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_USER_ID = "user_id";

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private TextView tvNavProfile, tvNavHome, tvNavCalendar, tvNavAi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_ui);

        // 🔥 정확한 알람 권한 확인 추가 (이 부분 새로 추가)
        checkExactAlarmPermission();

        // ✅ SharedPreferences에서 사용자 ID 가져오기
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "사용자 정보가 없습니다. 로그인 후 이용해주세요.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "User ID not found in SharedPreferences.");
        }
        Log.d(TAG, "Main_UI - Current User ID: " + currentUserId);

        // ✅ 알림 권한 요청 및 알람 예약
        NotificationHelper.requestNotificationPermission(this);
        if (currentUserId != -1) {
            NotificationHelper.scheduleNextAlarm(this, currentUserId);
        }

        spaceGrid = findViewById(R.id.spaceGrid);
        todoListLayout = findViewById(R.id.todoListLayout);

        // ✅ API 서비스 초기화
        spaceApiService = ApiClient.getSpaceApi();
        if (spaceApiService == null) {
            Log.e(TAG, "SpaceApi service could not be initialized.");
            Toast.makeText(this, "네트워크 서비스 초기화 오류", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ 결과 처리용 런처
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && currentUserId != -1) {
                        fetchSpacesFromServer(currentUserId);
                    }
                });

        // ✅ 공간 추가 버튼
        findViewById(R.id.btnAddSpace).setOnClickListener(v -> {
            if (currentUserId == -1) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, SpaceAddActivity.class);
            intent.putExtra("userId", currentUserId);
            activityResultLauncher.launch(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        setupBottomNavigation();

        // ✅ 서버 데이터 호출
        if (currentUserId != -1) {
            fetchSpacesFromServer(currentUserId);
            fetchTodaysRoutines(currentUserId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != -1 && spaceApiService != null) {
            fetchSpacesFromServer(currentUserId);
        }
    }

    // ✅ 오늘 루틴 불러오기
    private void fetchTodaysRoutines(int userId) {
        CleaningRoutineApi routineApi = ApiClient.getClient().create(CleaningRoutineApi.class);
        routineApi.getTodaysRoutines(userId).enqueue(new Callback<List<CleaningRoutine>>() {
            @Override
            public void onResponse(Call<List<CleaningRoutine>> call, Response<List<CleaningRoutine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todoListLayout.removeAllViews();
                    for (CleaningRoutine routine : response.body()) {
                        addTodoItem(routine);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<CleaningRoutine>> call, Throwable t) {
                Toast.makeText(Main_UI.this, "오늘 루틴 불러오기 실패", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "fetchTodaysRoutines failure: ", t);
            }
        });
    }

    // ✅ 공간 리스트 불러오기
    private void fetchSpacesFromServer(int userId) {
        spaceApiService.getSpacesByUserId(userId).enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(Call<List<Space>> call, Response<List<Space>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    spaceGrid.removeAllViews();
                    for (Space space : response.body()) {
                        addSpaceCard(space);
                    }
                } else {
                    handleApiError(response, "공간 목록 불러오기 실패");
                }
            }

            @Override
            public void onFailure(Call<List<Space>> call, Throwable t) {
                handleApiFailure(t, "공간 목록 서버 연결 오류");
            }
        });
    }

    private void addSpaceCard(final Space space) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setBackgroundColor(0xFFDADADA);
        container.setPadding(16, 24, 16, 24);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_room);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(96, 96);
        icon.setLayoutParams(iconParams);

        TextView tvName = new TextView(this);
        tvName.setText(space.getName());
        tvName.setGravity(Gravity.CENTER);
        tvName.setPadding(0, 8, 0, 0);

        container.addView(icon);
        container.addView(tvName);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 8, 8, 8);
        container.setLayoutParams(params);

        container.setOnClickListener(v -> {
            Intent intent = new Intent(this, CleaningList_UI.class);
            intent.putExtra("space_name", space.getName());
            intent.putExtra("space_id", space.getSpace_id());
            intent.putExtra("userId", currentUserId);
            startActivity(intent);
        });

        spaceGrid.addView(container);
    }

    // ✅ 수정된 addTodoItem 함수 (완료 버튼 포함)
    private void addTodoItem(CleaningRoutine routine) {
        View itemView = getLayoutInflater().inflate(R.layout.item_task, null);
        TextView tvContent = itemView.findViewById(R.id.tvContent);
        Button btnComplete = itemView.findViewById(R.id.btnComplete);
        tvContent.setText(routine.getTitle());

        final boolean[] isCompleted = {false};

        btnComplete.setOnClickListener(v -> {
            isCompleted[0] = !isCompleted[0];
            boolean nowCompleted = isCompleted[0];

            btnComplete.setText(nowCompleted ? "완료됨" : "완료");
            btnComplete.setBackgroundColor(nowCompleted ? Color.LTGRAY : Color.parseColor("#FF6200EE"));

            // ✅ 모델 객체로 요청 보내기
            CompleteRoutineRequest req = new CompleteRoutineRequest(routine.getRoutine_id(), nowCompleted);

            CleaningRoutineApi api = ApiClient.getClient().create(CleaningRoutineApi.class);
            api.toggleRoutineComplete(req).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("RoutineToggle", "성공");
                    } else {
                        Log.e("RoutineToggle", "실패 코드: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("RoutineToggle", "에러", t);
                }
            });
        });


        todoListLayout.addView(itemView);
    }


    private void setupBottomNavigation() {
        LinearLayout navProfile = findViewById(R.id.navProfile);
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navAi = findViewById(R.id.navAi);

        tvNavProfile = findViewById(R.id.navProfileText);
        tvNavHome = findViewById(R.id.navHomeText);
        tvNavCalendar = findViewById(R.id.navCalendarText);
        tvNavAi = findViewById(R.id.navAiText);

        Runnable resetTabColors = () -> {
            int gray = getResources().getColor(android.R.color.darker_gray, getTheme());
            tvNavProfile.setTextColor(gray);
            tvNavHome.setTextColor(gray);
            tvNavCalendar.setTextColor(gray);
            tvNavAi.setTextColor(gray);
        };

        resetTabColors.run();
        tvNavHome.setTextColor(getResources().getColor(android.R.color.black, getTheme()));

        navProfile.setOnClickListener(v -> {
            resetTabColors.run();
            tvNavProfile.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            navigateTo(Profile_UI.class);
        });

        navHome.setOnClickListener(v -> {
            resetTabColors.run();
            tvNavHome.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            if (currentUserId != -1 && spaceApiService != null) fetchSpacesFromServer(currentUserId);
        });

        navCalendar.setOnClickListener(v -> {
            resetTabColors.run();
            tvNavCalendar.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            navigateTo(CalendarActivity.class);
        });

        navAi.setOnClickListener(v -> {
            resetTabColors.run();
            tvNavAi.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            Intent intent = new Intent(this, RoutineMainActivity.class);
            intent.putExtra("userId", currentUserId);
            startActivity(intent);
        });
    }

    private void navigateTo(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }

    private void handleApiError(Response<?> response, String defaultMessage) {
        String errorMessage = defaultMessage + " (코드: " + response.code() + ")";
        if (response.errorBody() != null) {
            try {
                errorMessage += "\n 내용: " + response.errorBody().string();
            } catch (IOException e) {
                Log.e(TAG, "Error body parsing error", e);
            }
        }
        Log.e(TAG, errorMessage);
        Toast.makeText(this, defaultMessage, Toast.LENGTH_LONG).show();
    }

    private void handleApiFailure(Throwable t, String defaultMessage) {
        String failMessage = defaultMessage + ": " + t.getMessage();
        Log.e(TAG, failMessage, t);
        Toast.makeText(this, defaultMessage, Toast.LENGTH_LONG).show();
    }
    // 이건 Main_UI 클래스 가장 아래쪽에 추가하면 좋아요

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }
}