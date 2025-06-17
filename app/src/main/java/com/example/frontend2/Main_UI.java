package com.example.frontend2;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;
import java.util.HashMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class Main_UI extends AppCompatActivity {

    private static final String TAG = "Main_UI";
    private GridLayout spaceGrid;
    private LinearLayout todoListLayout;
    private SpaceApi spaceApiService;
    private int currentUserId = -1;

    // SharedPreferences 키 통일 (Login_UI와 맞춤)
    public static final String PREFS_NAME = "CleanItAppPrefs";
    public static final String KEY_USER_ID = "logged_in_user_id";
    public static final String KEY_USER_NAME = "user_name";

    private ActivityResultLauncher<Intent> activityResultLauncher;

    private TextView tvNavProfile, tvNavHome, tvNavCalendar, tvNavAi;


    private static final Map<String, String> spaceEmojiMap = new HashMap<>();
    static {
        spaceEmojiMap.put("거실", "🛋️");
        spaceEmojiMap.put("침실", "🛏️");
        spaceEmojiMap.put("부엌", "🍳");
        spaceEmojiMap.put("화장실", "🚽");
        spaceEmojiMap.put("세탁실", "🧺");
        spaceEmojiMap.put("옷방", "👗");
        spaceEmojiMap.put("현관", "🚪");
        spaceEmojiMap.put("서재", "📚");
        spaceEmojiMap.put("다용도실", "🧹");
        spaceEmojiMap.put("베란다", "🌿");
        spaceEmojiMap.put("아이방", "🧸");
        spaceEmojiMap.put("펫룸", "🐶");
        spaceEmojiMap.put("차고", "🚗");
        spaceEmojiMap.put("창고", "📦");
        spaceEmojiMap.put("테라스", "☀️");
        spaceEmojiMap.put("기타", "❓");
    }

    private String getEmojiForSpaceType(String type) {
        return spaceEmojiMap.getOrDefault(type, "❓");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_ui);

        spaceGrid = findViewById(R.id.spaceGrid);
        todoListLayout = findViewById(R.id.todoListLayout);

        checkExactAlarmPermission();

        // ✅ SharedPreferences에서 항상 로그인된 최신 userId 읽어오기
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "사용자 정보가 없습니다. 로그인 후 이용해주세요.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "User ID not found in SharedPreferences.");
            return;
        }
        Log.d(TAG, "Main_UI - Current User ID: " + currentUserId);

        NotificationHelper.requestNotificationPermission(this);
        NotificationHelper.scheduleNextAlarm(this, currentUserId);

        spaceApiService = ApiClient.getSpaceApi();
        if (spaceApiService == null) {
            Log.e(TAG, "SpaceApi service could not be initialized.");
            Toast.makeText(this, "네트워크 서비스 초기화 오류", Toast.LENGTH_SHORT).show();
            return;
        }

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "Returned from activity with RESULT_OK. Refreshing spaces.");
                        fetchSpacesFromServer(currentUserId);
                    }
                }
        );

        findViewById(R.id.btnAddSpace).setOnClickListener(v -> {
            Intent intent = new Intent(Main_UI.this, SpaceListActivity.class);
            intent.putExtra("userId", currentUserId);
            activityResultLauncher.launch(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        setupBottomNavigation();

        fetchSpacesFromServer(currentUserId);
        fetchTodaysRoutines(currentUserId);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ 항상 화면 복귀 시 SharedPreferences에서 최신 userId 다시 확인
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, -1);

        if (currentUserId != -1) {
            Log.d(TAG, "onResume: Refreshing spaces for userId: " + currentUserId);
            fetchSpacesFromServer(currentUserId);
        }
    }

    private void fetchTodaysRoutines(int userId) {
        CleaningRoutineApi routineApi = ApiClient.getClient().create(CleaningRoutineApi.class);
        routineApi.getTodaysRoutines(userId).enqueue(new Callback<List<CleaningRoutine>>() {
            @Override
            public void onResponse(Call<List<CleaningRoutine>> call, Response<List<CleaningRoutine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "받은 루틴 개수: " + response.body().size());
                    todoListLayout.removeAllViews();
                    for (CleaningRoutine routine : response.body()) {
                        Log.d(TAG, "루틴 제목: " + routine.getTitle());
                        addTodoItem(routine);
                    }
                } else {
                    Log.e(TAG, "응답은 성공했지만 데이터 없음: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CleaningRoutine>> call, Throwable t) {
                Toast.makeText(Main_UI.this, "오늘 루틴 불러오기 실패", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "fetchTodaysRoutines failure: ", t);
            }
        });
    }


    private void fetchSpacesFromServer(int userId) {
        Log.d(TAG, "fetchSpacesFromServer: userId = " + userId);
        spaceApiService.getSpacesByUserId(userId).enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(Call<List<Space>> call, Response<List<Space>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    spaceGrid.removeAllViews();
                    List<Space> spaces = response.body();
                    Log.d(TAG, "공간 목록 로드 성공: " + spaces.size());
                    if (spaces.isEmpty()) {
                        Toast.makeText(Main_UI.this, "등록된 공간이 없습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        for (Space space : spaces) {
                            addSpaceCard(space);
                        }
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
        View cardView = getLayoutInflater().inflate(R.layout.item_space_card, null);

        TextView tvEmoji = cardView.findViewById(R.id.tvEmoji);
        TextView tvSpaceName = cardView.findViewById(R.id.tvSpaceName);

        String emoji = getEmojiForSpaceType(space.getType());
        tvEmoji.setText(emoji);
        tvSpaceName.setText(space.getName());

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(params);

        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(Main_UI.this, CleaningList_UI.class);
            intent.putExtra("space_name", space.getName());
            intent.putExtra("space_id", space.getSpace_id());
            intent.putExtra("userId", currentUserId);
            startActivity(intent);
        });

        spaceGrid.addView(cardView);
    }



    private void addTodoItem(CleaningRoutine routine) {
        View itemView = getLayoutInflater().inflate(R.layout.item_task, null);
        TextView tvContent = itemView.findViewById(R.id.tvContent);
        Button btnComplete = itemView.findViewById(R.id.btnComplete);
        tvContent.setText(routine.getTitle());

        // ✅ 초기에 상태 파악 가능하도록 변수로 상태관리
        final boolean[] isCompleted = {false};

        btnComplete.setOnClickListener(v -> {
            isCompleted[0] = !isCompleted[0];
            boolean nowCompleted = isCompleted[0];

            // ✅ 색상 및 텍스트 변경만
            btnComplete.setText(nowCompleted ? "완료됨" : "완료");
            btnComplete.setTextColor(Color.WHITE);
            btnComplete.setBackgroundColor(nowCompleted ? Color.parseColor("#999999") : Color.parseColor("#FF6200EE"));

            // ✅ 서버에도 완료 상태 업데이트
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

        // ✅ 항상 리스트에 유지되도록 그냥 추가만
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
            navigateTo(Profile_UI.class, false);
        });
        navHome.setOnClickListener(v -> {
            resetTabColors.run();
            tvNavHome.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            fetchSpacesFromServer(currentUserId);
        });
        navCalendar.setOnClickListener(v -> {
            resetTabColors.run();
            tvNavCalendar.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            navigateTo(CalendarActivity.class, false);
        });
        navAi.setOnClickListener(v -> {
            resetTabColors.run();
            tvNavAi.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            Intent intent = new Intent(this, RoutineMainActivity.class);
            intent.putExtra("userId", currentUserId);
            startActivity(intent);
        });
    }

    private void navigateTo(Class<?> destinationActivity, boolean finishCurrent) {
        Intent intent = new Intent(Main_UI.this, destinationActivity);
        startActivity(intent);
        if (finishCurrent) {
            finish();
        }
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

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }
}
