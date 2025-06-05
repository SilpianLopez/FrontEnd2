package com.example.frontend2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // ActivityResultLauncher 사용
import androidx.activity.result.contract.ActivityResultContracts; // ActivityResultLauncher 사용
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
// import androidx.appcompat.widget.Toolbar; // Toolbar 사용 시

import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.SpaceApi;
import com.example.frontend2.models.Space;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutineMainActivity extends AppCompatActivity {

    private static final String TAG = "RoutineMainActivity";
    public static final int SPACE_ID_FOR_ALL_RECOMMENDATIONS = 0;

    private List<Space> userSpaceList = new ArrayList<>();
    private SpaceApi spaceApiService;
    private int currentUserId = -1;

    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_USER_ID = "current_user_id";

    private Button btnAllRoutine;
    private LinearLayout routineButtonContainer;
    private TextView tvNoSpacesMessage;
    private TextView tvLoadingMessage; // 로딩 메시지 표시용 (XML에 추가 또는 동적 생성)

    // 다른 Activity에서 결과를 받아오기 위한 Launcher
    private ActivityResultLauncher<Intent> routineActivityResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_main);

        // Toolbar toolbar = findViewById(R.id.myToolbar); // XML에 Toolbar가 있다면
        // setSupportActionBar(toolbar);
        // if (getSupportActionBar() != null) {
        //     getSupportActionBar().setTitle("AI 추천 선택");
        // }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "로그인이 필요합니다. 테스트용 ID(1)를 사용합니다.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "User not logged in. Using temporary user ID 1.");
            currentUserId = 1; // 실제 앱에서는 로그인 화면으로
        }
        Log.d(TAG, "Current User ID: " + currentUserId);

        btnAllRoutine = findViewById(R.id.btnAllRoutine);
        routineButtonContainer = findViewById(R.id.routineButtonContainer);
        tvNoSpacesMessage = findViewById(R.id.tvNoSpacesMessage); // XML ID 확인
        // tvLoadingMessage = findViewById(R.id.tvLoadingMessage); // XML에 있다면

        if (ApiClient.getSpaceApi() != null) {
            spaceApiService = ApiClient.getSpaceApi();
        } else {
            Log.e(TAG, "SpaceApi service is null. Check ApiClient setup.");
            Toast.makeText(this, "네트워크 서비스 초기화 오류", Toast.LENGTH_SHORT).show();
            if (tvNoSpacesMessage != null) tvNoSpacesMessage.setVisibility(View.VISIBLE);
            return;
        }

        // ActivityResultLauncher 초기화 (RoutineAllActivity 또는 RoutineDetailActivity에서 돌아올 때)
        routineActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        // AI 추천이 성공적으로 사용자의 루틴에 반영되었을 때
                        Log.d(TAG, "AI 추천이 성공적으로 루틴에 반영되었습니다. 목록을 새로고침합니다 (필요시).");
                        // 여기서 RoutineList_UI로 이동했다면 그 화면이 onResume 등에서 새로고침 할 것임
                        // 현재 화면(RoutineMainActivity)에서 특별히 새로고침할 내용이 없다면 Toast만 표시 가능
                        Toast.makeText(this, "루틴이 성공적으로 반영되었습니다!", Toast.LENGTH_SHORT).show();
                        // fetchUserSpacesAndCreateDynamicButtons(currentUserId); // 공간 목록이 변경될 수 있으므로 다시 로드
                    }
                });

        setupNavigationAndGlobalButtons();

        if (currentUserId != -1) {
            fetchUserSpacesAndCreateDynamicButtons(currentUserId);
        } else {
            displayNoSpacesMessage("로그인 정보가 없어 공간을 표시할 수 없습니다.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != -1 && spaceApiService != null) {
            Log.d(TAG, "onResume: Refreshing user spaces for userId: " + currentUserId);
            fetchUserSpacesAndCreateDynamicButtons(currentUserId);
        }
    }

    private void setupNavigationAndGlobalButtons() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navAi = findViewById(R.id.navAi);

        navHome.setOnClickListener(v -> navigateTo(Main_UI.class, true));
        navCalendar.setOnClickListener(v -> navigateTo(CalendarActivity.class, true));
        navAi.setOnClickListener(v -> { /* 현재 화면 */ });

        btnAllRoutine.setOnClickListener(v -> {
            if (currentUserId == -1) { /* 로그인 체크 */ return; }
            // RoutineAllActivity 호출
            startNextAiActivity("전체 공간", SPACE_ID_FOR_ALL_RECOMMENDATIONS, currentUserId, RoutineAllActivity.class);
        });
    }

    private void fetchUserSpacesAndCreateDynamicButtons(int userId) {
        Log.d(TAG, "사용자 공간 목록 요청 시작, userId: " + userId);
        if (tvNoSpacesMessage != null) tvNoSpacesMessage.setVisibility(View.GONE);
        routineButtonContainer.removeAllViews();
        if (tvLoadingMessage != null) {
            tvLoadingMessage.setText("공간 목록을 불러오는 중...");
            tvLoadingMessage.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "공간 목록 로딩 중...", Toast.LENGTH_SHORT).show();
        }

        Call<List<Space>> call = spaceApiService.getSpacesByUserId(userId);
        call.enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(@NonNull Call<List<Space>> call, @NonNull Response<List<Space>> response) {
                if (tvLoadingMessage != null) tvLoadingMessage.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    userSpaceList.clear();
                    userSpaceList.addAll(response.body());
                    Log.d(TAG, "사용자 공간 목록 로드 성공: " + userSpaceList.size() + "개");
                    createDynamicSpaceButtons();
                } else {
                    handleApiError(response, "공간 목록 로드 실패");
                    displayNoSpacesMessage("등록된 공간 정보를 가져오는 데 실패했습니다.");
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Space>> call, @NonNull Throwable t) {
                if (tvLoadingMessage != null) tvLoadingMessage.setVisibility(View.GONE);
                handleApiFailure(t, "공간 목록 API 호출 오류");
                displayNoSpacesMessage("네트워크 오류로 공간 목록을 가져올 수 없습니다.");
            }
        });
    }

    private void createDynamicSpaceButtons() {
        routineButtonContainer.removeAllViews();
        if (userSpaceList == null || userSpaceList.isEmpty()) {
            Log.w(TAG, "동적으로 생성할 사용자 공간 정보가 없습니다.");
            displayNoSpacesMessage("등록된 공간이 없습니다. '+' 버튼을 눌러 공간을 추가해주세요.");
            return;
        }
        if (tvNoSpacesMessage != null) tvNoSpacesMessage.setVisibility(View.GONE);

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int topMarginInDp = 16; int bottomMarginInDp = 8;
        final float scale = getResources().getDisplayMetrics().density;
        buttonLayoutParams.setMargins(0, (int) (topMarginInDp * scale + 0.5f), 0, (int) (bottomMarginInDp * scale + 0.5f));

        for (final Space space : userSpaceList) {
            Button spaceButton = new Button(this);
            spaceButton.setText(space.getName() + " AI 추천");
            spaceButton.setLayoutParams(buttonLayoutParams);
            spaceButton.setBackgroundResource(R.drawable.btn_outline_black);
            spaceButton.setTextColor(getResources().getColor(android.R.color.black, null));
            spaceButton.setTag(space);

            spaceButton.setOnClickListener(view -> {
                Object tag = view.getTag();
                if (tag instanceof Space) {
                    Space selectedSpace = (Space) tag;
                    if (currentUserId != -1) {
                        Log.d(TAG, "동적 공간 버튼 클릭: " + selectedSpace.getName() + " (ID: " + selectedSpace.getSpace_id() + ")");
                        startNextAiActivity(selectedSpace.getName(), selectedSpace.getSpace_id(), currentUserId, RoutineDetailActivity.class);
                    }
                }
            });
            routineButtonContainer.addView(spaceButton);
        }
    }

    private void displayNoSpacesMessage(String message) {
        routineButtonContainer.removeAllViews();
        if (tvNoSpacesMessage != null) {
            tvNoSpacesMessage.setText(message);
            tvNoSpacesMessage.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void startNextAiActivity(String roomName, int spaceId, int userId, Class<?> destinationActivityClass) {
        Intent intent = new Intent(this, destinationActivityClass);
        intent.putExtra("roomName", roomName);
        intent.putExtra("spaceId", spaceId);
        intent.putExtra("userId", userId);
        routineActivityResultLauncher.launch(intent); // 결과를 받기 위해 Launcher 사용
    }
    private void navigateTo(Class<?> destinationActivity, boolean finishCurrent) {
        Intent intent = new Intent(RoutineMainActivity.this, destinationActivity);
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
        Toast.makeText(this, defaultMessage + " (상세 내용은 로그 확인)", Toast.LENGTH_LONG).show();
    }

    private void handleApiFailure(Throwable t, String defaultMessage) {
        String failMessage = defaultMessage + ": " + t.getMessage();
        Log.e(TAG, failMessage, t);
        Toast.makeText(this, defaultMessage + " (상세 내용은 로그 확인)", Toast.LENGTH_LONG).show();
    }
}