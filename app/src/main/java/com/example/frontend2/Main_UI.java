package com.example.frontend2;

import android.content.Context; // SharedPreferences 용
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color; // 하단 탭 색상 변경에 필요 (기존 코드 유지)
import android.os.Bundle;
import android.util.Log;    // 로그 사용
// import android.util.TypedValue; // UI 변경 없으므로 주석 처리 또는 이전 값 유지
import android.view.Gravity;
import android.view.View;    // View.OnClickListener 용
import android.view.ViewGroup;
// import android.widget.Button; // btnAddSpace가 Button일 경우 (현재는 View로 받음)
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // ActivityResultLauncher 사용
import androidx.activity.result.contract.ActivityResultContracts; // ActivityResultLauncher 사용
import androidx.annotation.NonNull; // NonNull 사용
import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.SpaceApi;
import com.example.frontend2.models.Space;

import java.io.IOException; // 에러 처리용
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main_UI extends AppCompatActivity {

    private static final String TAG = "Main_UI"; // 로그용 태그
    private GridLayout spaceGrid;
    private LinearLayout todoListLayout;
    private SpaceApi spaceApiService;
    private int currentUserId = -1;   // 현재 사용자 ID

    // SharedPreferences 상수 (다른 Activity와 일관성 유지)
    public static final String PREFS_NAME = "UserPrefs"; // 예시 이름, 실제 사용하는 이름으로 변경
    public static final String KEY_USER_ID = "current_user_id"; // 예시 키, 실제 사용하는 키로 변경

    // SpaceAddActivity 또는 다른 Activity에서 결과를 받아오기 위한 Launcher
    private ActivityResultLauncher<Intent> activityResultLauncher;

    // 하단 네비게이션 텍스트뷰 참조 (UI 변경 없으므로 색상 변경 로직은 그대로 둠)
    private TextView tvNavProfile, tvNavHome, tvNavCalendar, tvNavAi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_ui);

        spaceGrid = findViewById(R.id.spaceGrid);
        todoListLayout = findViewById(R.id.todoListLayout);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE); // PREFS_NAME 사용
        currentUserId = prefs.getInt(KEY_USER_ID, -1);  // KEY_USER_ID 사용

        if (currentUserId == -1) {
            Toast.makeText(this, "사용자 정보가 없습니다. 로그인 후 이용해주세요.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "User ID not found in SharedPreferences. Redirecting to Login (assumed).");
            // TODO: 실제 로그인 화면으로 이동하는 Intent 로직 필요
            // Intent loginIntent = new Intent(this, Login_UI.class);
            // startActivity(loginIntent);
            // finish();
            // 테스트를 위해 임시 ID 사용 (실제 앱에서는 로그인 필수)
            // currentUserId = 1;
            // if(currentUserId == -1) return;
            Log.w(TAG, "테스트를 위해 currentUserId가 -1인 상태로 진행될 수 있습니다. 실제 앱에서는 로그인 확인 필요.");
        }
        Log.d(TAG, "Main_UI - Current User ID: " + currentUserId);


        if (ApiClient.getSpaceApi() != null) {
            spaceApiService = ApiClient.getSpaceApi();
        } else {
            Log.e(TAG, "SpaceApi service could not be initialized from ApiClient.");
            Toast.makeText(this, "네트워크 서비스 초기화 오류", Toast.LENGTH_SHORT).show();
            return;
        }

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Log.d(TAG, "Returned from another activity with RESULT_OK. Refreshing data if needed.");
                        if (currentUserId != -1) {
                            fetchSpacesFromServer(currentUserId);
                        }
                    }
                });

        findViewById(R.id.btnAddSpace).setOnClickListener(v -> {
            if (currentUserId == -1) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Main_UI.this, SpaceAddActivity.class);
            intent.putExtra("userId", currentUserId);
            activityResultLauncher.launch(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        findViewById(R.id.btnAlarm).setOnClickListener(v -> {
            Intent intent = new Intent(Main_UI.this, AlarmActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        setupBottomNavigation();

        addTodoItem("청소 항목1");
        addTodoItem("청소 항목2");

        if (currentUserId != -1) {
            fetchSpacesFromServer(currentUserId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != -1 && spaceApiService != null) {
            Log.d(TAG, "onResume: Refreshing spaces for userId: " + currentUserId);
            fetchSpacesFromServer(currentUserId);
        }
    }

    private void fetchSpacesFromServer(int userId) {
        if (spaceApiService == null) {
            Log.e(TAG, "fetchSpacesFromServer: spaceApiService is null.");
            Toast.makeText(this, "공간 서비스 연결 오류", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "공간 목록 요청 API 호출, userId: " + userId);

        spaceApiService.getSpacesByUserId(userId).enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(Call<List<Space>> call, Response<List<Space>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    spaceGrid.removeAllViews();
                    List<Space> spaces = response.body();
                    Log.d(TAG, "공간 목록 로드 성공: " + spaces.size() + "개");
                    if (spaces.isEmpty()) {
                        Toast.makeText(Main_UI.this, "등록된 공간이 없습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        for (Space space : spaces) {
                            addSpaceCard(space); // ❗️ Space 객체 전체 전달
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

    // ❗️ 파라미터를 Space 객체로 변경
    private void addSpaceCard(final Space space) {
        // UI 요소 생성 및 설정은 기존 코드 유지 (모양 변경 없음)
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
        tvName.setText(space.getName()); // ❗️ Space 객체의 이름 사용
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

        // ❗️ CleaningList_UI로 space_id와 space_name, userId 전달
        container.setOnClickListener(v -> {
            Intent intent = new Intent(Main_UI.this, CleaningList_UI.class);
            intent.putExtra("space_name", space.getName());     // "space_name" 키로 전달
            intent.putExtra("space_id", space.getSpace_id());     // ◀ "space_id" 키로 전달 (핵심 수정)
            intent.putExtra("userId", currentUserId);           // 현재 사용자 ID도 전달 (CleaningList_UI에서 사용)
            startActivity(intent);
            // overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 필요시 사용
        });
        spaceGrid.addView(container);
    }

    private void addTodoItem(String content) {
        TextView tv = new TextView(this);
        tv.setText("· " + content);
        tv.setTextSize(14); // UI 변경 없도록 이전 값 유지
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8); // UI 변경 없도록 이전 값 유지
        tv.setLayoutParams(params);
        todoListLayout.addView(tv);
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

        // 색 초기화 함수 (기존 로직 유지)
        Runnable resetTabColors = () -> {
            int gray = getResources().getColor(android.R.color.darker_gray, getTheme()); // API 23+
            tvNavProfile.setTextColor(gray);
            tvNavHome.setTextColor(gray);
            tvNavCalendar.setTextColor(gray);
            tvNavAi.setTextColor(gray);
        };

        resetTabColors.run();
        tvNavHome.setTextColor(getResources().getColor(android.R.color.black, getTheme())); // API 23+
        tvNavHome.setTextColor(getResources().getColor(android.R.color.black, getTheme())); // API 23+

        navProfile.setOnClickListener(v -> {
            resetTabColors.run();
            tvNavProfile.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            navigateTo(Profile_UI.class, false);
        });
        navHome.setOnClickListener(v -> {
            resetTabColors.run();
            tvNavHome.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            if (currentUserId != -1 && spaceApiService != null) fetchSpacesFromServer(currentUserId);
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
            intent.putExtra("userId", currentUserId); // userId 전달
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

    // 공통 에러 처리 헬퍼 함수들
    private void handleApiError(Response<?> response, String defaultMessage) {
        String errorMessage = defaultMessage + " (코드: " + response.code() + ")";
        if (response.errorBody() != null) {
            try {
                errorMessage += "\n 내용: " + response.errorBody().string();
            } catch (IOException e) { Log.e(TAG, "Error body parsing error", e); }
        }
        Log.e(TAG, errorMessage);
        Toast.makeText(this, defaultMessage, Toast.LENGTH_LONG).show();
    }

    private void handleApiFailure(Throwable t, String defaultMessage) {
        String failMessage = defaultMessage + ": " + t.getMessage();
        Log.e(TAG, failMessage, t);
        Toast.makeText(this, defaultMessage, Toast.LENGTH_LONG).show();
    }
}