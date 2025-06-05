package com.example.frontend2; // ❗️ 실제 프로젝트의 패키지 경로로 반드시 수정하세요.

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity; // Gravity 사용 시 필요할 수 있음 (여기서는 직접 사용 안 함)
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Toolbar를 사용한다면 import

// ❗️ 실제 프로젝트의 경로로 모두 수정하세요.
import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.SpaceApi;
import com.example.frontend2.models.Space; // Space 모델 import

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutineMainActivity extends AppCompatActivity {

    private static final String TAG = "RoutineMainActivity";
    // "전체 추천" 요청 시 RoutineAllActivity 또는 RoutineDetailActivity로 전달할 spaceId
    public static final int SPACE_ID_FOR_ALL_RECOMMENDATIONS = 0;

    private List<Space> userSpaceList = new ArrayList<>();
    private SpaceApi spaceApiService;
    private int currentUserId = -1; // 로그인된 사용자 ID, 유효하지 않으면 -1

    // SharedPreferences 이름 및 키 (앱 전체에서 일관되게 사용하는 것이 좋음)
    public static final String PREFS_NAME = "UserPrefs"; // 예시 이름, 실제 사용하는 이름으로 변경
    public static final String KEY_USER_ID = "current_user_id"; // 예시 키, 실제 사용하는 키로 변경

    private Button btnAllRoutine;
    private LinearLayout routineButtonContainer;
    private TextView tvNoSpacesMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_main); // activity_routine_main.xml 사용

        // Toolbar 설정 (XML에 Toolbar ID가 myToolbar라고 가정하고 추가)
        // Toolbar toolbar = findViewById(R.id.myToolbar); // XML에 Toolbar가 있다면 ID 확인
        // setSupportActionBar(toolbar);
        // if (getSupportActionBar() != null) {
        //     // getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 필요하다면 뒤로가기 버튼
        //     getSupportActionBar().setTitle("AI 추천 공간 선택"); // 화면 제목 설정
        // }

        // 1. 로그인된 사용자 ID 가져오기
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "로그인이 필요합니다. 로그인 화면으로 이동합니다.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "User not logged in. currentUserId: " + currentUserId + ". Redirecting to Login.");
            // TODO: 실제 로그인 화면(예: Login_UI.class)으로 이동하는 Intent 로직 추가
            // Intent loginIntent = new Intent(this, Login_UI.class);
            // startActivity(loginIntent);
            finish(); // 현재 액티비티 종료
            return; // onCreate 더 이상 진행 안 함
        }
        Log.d(TAG, "Current User ID: " + currentUserId);

        // 2. UI 요소 초기화
        btnAllRoutine = findViewById(R.id.btnAllRoutine);
        routineButtonContainer = findViewById(R.id.routineButtonContainer);
        tvNoSpacesMessage = findViewById(R.id.tvNoSpacesMessage); // XML의 ID와 일치해야 함

        // 3. Retrofit API 서비스 초기화
        if (ApiClient.getClient() != null) {
            spaceApiService = ApiClient.getClient().create(SpaceApi.class);
        } else {
            Log.e(TAG, "ApiClient.getClient() is null. Check Retrofit setup.");
            Toast.makeText(this, "네트워크 초기화 오류입니다. 앱을 재시작해주세요.", Toast.LENGTH_SHORT).show();
            if (tvNoSpacesMessage != null) { // tvNoSpacesMessage가 null이 아닐 때만 접근
                tvNoSpacesMessage.setText("네트워크 초기화 오류입니다.");
                tvNoSpacesMessage.setVisibility(View.VISIBLE);
            }
            return; // API 서비스 초기화 실패 시 더 이상 진행하지 않음
        }

        // 4. 네비게이션 바 및 "AI 전체 공간 추천" 버튼 리스너 설정
        setupNavigationAndGlobalButtons();

        // 5. 사용자 공간 목록을 서버에서 가져와 버튼 동적 생성
        //    onResume에서도 호출하여 다른 화면에서 공간 추가/삭제 후 돌아왔을 때 목록을 갱신할 수 있도록 고려
        fetchUserSpacesAndCreateDynamicButtons(currentUserId);
    }

    // 화면이 다시 보여질 때마다 공간 목록을 새로고침할 수 있도록 onResume에 추가 (선택 사항)
    @Override
    protected void onResume() {
        super.onResume();
        // 로그인 상태 및 사용자 ID를 다시 확인하고, 유효하다면 공간 목록 새로고침
        if (currentUserId != -1 && spaceApiService != null) {
            Log.d(TAG, "onResume: Fetching user spaces again for userId: " + currentUserId);
            fetchUserSpacesAndCreateDynamicButtons(currentUserId);
        } else if (currentUserId == -1) {
            // 혹시 로그인 상태가 변경되었을 수 있으므로 다시 체크
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            currentUserId = prefs.getInt(KEY_USER_ID, -1);
            if (currentUserId == -1) {
                Log.e(TAG, "onResume: User still not logged in.");
                // 로그인 화면으로 보내는 로직을 여기서도 고려
            } else {
                fetchUserSpacesAndCreateDynamicButtons(currentUserId);
            }
        }
    }


    private void setupNavigationAndGlobalButtons() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navAi = findViewById(R.id.navAi); // 이 버튼은 현재 Activity를 다시 로드할 수도 있음

        navHome.setOnClickListener(v -> navigateTo(Main_UI.class, true));
        navCalendar.setOnClickListener(v -> navigateTo(CalendarActivity.class, true));
        navAi.setOnClickListener(v -> {
            // 현재 화면이 AI 추천 기능의 메인 선택 화면이므로,
            // 클릭 시 새로고침 (fetchUserSpacesAndCreateDynamicButtons) 또는 다른 AI 관련 화면으로 이동 가능
            Toast.makeText(this, "AI 추천 기능 선택 화면입니다.", Toast.LENGTH_SHORT).show();
            if (currentUserId != -1) fetchUserSpacesAndCreateDynamicButtons(currentUserId); // 새로고침 예시
        });

        btnAllRoutine.setOnClickListener(v -> {
            if (currentUserId == -1) {
                Toast.makeText(this, "로그인 정보가 없어 전체 추천을 받을 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            // RoutineAllActivity (AI 전체 추천 결과 화면)로 이동
            startNextActivity("전체 공간", SPACE_ID_FOR_ALL_RECOMMENDATIONS, currentUserId, RoutineAllActivity.class);
        });
    }

    private void fetchUserSpacesAndCreateDynamicButtons(int userId) {
        Log.d(TAG, "사용자 공간 목록 요청 시작, userId: " + userId);
        if (tvNoSpacesMessage != null) tvNoSpacesMessage.setVisibility(View.GONE); // API 호출 시작 시 메시지 숨김
        routineButtonContainer.removeAllViews(); // 이전 동적 버튼들 제거
        // TODO: 여기에 로딩 인디케이터(ProgressBar)를 보이도록 하는 코드 추가 (예: ProgressBar.setVisibility(View.VISIBLE))

        Call<List<Space>> call = spaceApiService.getSpacesByUserId(userId);
        call.enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(@NonNull Call<List<Space>> call, @NonNull Response<List<Space>> response) {
                // TODO: 로딩 인디케이터 숨기기
                if (response.isSuccessful() && response.body() != null) {
                    userSpaceList.clear(); // 이전 데이터 클리어
                    userSpaceList.addAll(response.body());
                    Log.d(TAG, "사용자 공간 목록 로드 성공: " + userSpaceList.size() + "개");
                    createDynamicSpaceButtons(); // 가져온 공간 정보로 버튼 동적 생성
                } else {
                    handleApiError(response, "공간 목록 로드 실패");
                    if (tvNoSpacesMessage != null) {
                        tvNoSpacesMessage.setText("등록된 공간 정보를 가져오는 데 실패했습니다.");
                        tvNoSpacesMessage.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Space>> call, @NonNull Throwable t) {
                // TODO: 로딩 인디케이터 숨기기
                handleApiFailure(t, "공간 목록 API 호출 오류");
                if (tvNoSpacesMessage != null) {
                    tvNoSpacesMessage.setText("네트워크 오류로 공간 목록을 가져올 수 없습니다.");
                    tvNoSpacesMessage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void createDynamicSpaceButtons() {
        routineButtonContainer.removeAllViews(); // 안전하게 다시 한번 기존 뷰들 제거 (tvNoSpacesMessage 포함될 수 있음)

        if (userSpaceList == null || userSpaceList.isEmpty()) {
            Log.w(TAG, "동적으로 생성할 사용자 공간 정보가 없습니다.");
            if (tvNoSpacesMessage != null) {
                tvNoSpacesMessage.setText("등록된 공간이 없습니다. '+' 버튼을 눌러 공간을 추가해주세요.");
                tvNoSpacesMessage.setVisibility(View.VISIBLE);
                // XML에 tvNoSpacesMessage가 routineButtonContainer의 자식으로 있다면,
                // routineButtonContainer.removeAllViews() 후에 다시 추가해줘야 할 수 있습니다.
                // 여기서는 XML에서 tvNoSpacesMessage가 routineButtonContainer 밖에 있다고 가정하거나,
                // 이 함수 내에서 필요시 다시 routineButtonContainer에 추가합니다.
                // 가장 간단한 방법은 XML에서 tvNoSpacesMessage를 routineButtonContainer와 별개로 두는 것입니다.
                // 여기서는 XML에 이미 포함되어 있다고 가정하고 visibility만 제어합니다.
            }
            return;
        }

        if (tvNoSpacesMessage != null) tvNoSpacesMessage.setVisibility(View.GONE); // 공간이 있으므로 메시지 숨김

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int topMarginInDp = 16;
        int bottomMarginInDp = 8;
        final float scale = getResources().getDisplayMetrics().density;
        int topMarginInPx = (int) (topMarginInDp * scale + 0.5f);
        int bottomMarginInPx = (int) (bottomMarginInDp * scale + 0.5f);
        buttonLayoutParams.setMargins(0, topMarginInPx, 0, bottomMarginInPx);

        for (final Space space : userSpaceList) {
            Button spaceButton = new Button(this);
            spaceButton.setText(space.getName() + " AI 추천");
            spaceButton.setLayoutParams(buttonLayoutParams);
            spaceButton.setBackgroundResource(R.drawable.btn_outline_black);
            spaceButton.setTextColor(getResources().getColor(android.R.color.black, null)); // API 23+
            spaceButton.setTag(space); // 버튼 태그에 Space 객체 저장

            spaceButton.setOnClickListener(view -> {
                Object tag = view.getTag();
                if (tag instanceof Space) {
                    Space selectedSpace = (Space) tag;
                    if (currentUserId != -1) {
                        Log.d(TAG, "동적 공간 버튼 클릭: " + selectedSpace.getName() + " (ID: " + selectedSpace.getSpace_id() + ")");
                        // RoutineDetailActivity (특정 공간 추천 화면)로 이동
                        startNextActivity(selectedSpace.getName(), selectedSpace.getSpace_id(), currentUserId, RoutineDetailActivity.class);
                    } else {
                        Toast.makeText(this, "사용자 정보가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "클릭된 버튼의 태그에서 Space 객체를 찾을 수 없거나 타입이 다릅니다. Tag: " + tag);
                    Toast.makeText(this, "공간 정보를 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
            routineButtonContainer.addView(spaceButton);
        }
    }

    /**
     * AI 추천 관련 Activity를 시작하는 헬퍼 함수
     */
    private void startNextActivity(String roomName, int spaceId, int userId, Class<?> destinationActivityClass) {
        Intent intent = new Intent(this, destinationActivityClass);
        intent.putExtra("roomName", roomName);
        intent.putExtra("spaceId", spaceId);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    /**
     * 일반적인 Activity 이동 헬퍼 함수
     */
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