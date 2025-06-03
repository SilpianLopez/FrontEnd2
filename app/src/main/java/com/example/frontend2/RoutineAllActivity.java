package com.example.frontend2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.Recommendation;
import com.example.frontend2.models.RecommendationRequest;
import com.example.frontend2.models.Space;
import com.example.frontend2.api.AiRoutineApi;
import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.RoutineApi;
import com.example.frontend2.api.SpaceApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutineAllActivity extends AppCompatActivity {

    private static final String TAG = "RoutineAllActivity";
    private AiRoutineApi aiRoutineApiService;
    private RoutineApi routineApiService;
    private SpaceApi spaceApiService;

    private TextView tvRecommendationContent;
    private Button btnApplyRoutine;
    private Button btnRetryRoutine;

    private List<Recommendation> currentRecommendations = new ArrayList<>();
    private List<Space> userSpaceList = new ArrayList<>();
    private int currentUserId = -1;
    private String screenTitleTextFromIntent;

    public static final String PREFS_NAME_FOR_APP = "CleanItAppPrefs";
    public static final String KEY_USER_ID_FOR_APP = "logged_in_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_all);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            currentUserId = intent.getIntExtra("userId", -1);
            screenTitleTextFromIntent = intent.getStringExtra("roomName");

            // userId가 있다면 SharedPreferences에 저장
            if (currentUserId != -1) {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME_FOR_APP, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(KEY_USER_ID_FOR_APP, currentUserId);
                editor.apply();
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(screenTitleTextFromIntent != null && !screenTitleTextFromIntent.isEmpty()
                    ? screenTitleTextFromIntent + " AI 추천"
                    : "AI 전체 루틴 추천");
        }

        // SharedPreferences에서 userId 불러오기
        if (currentUserId == -1) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME_FOR_APP, MODE_PRIVATE);
            currentUserId = prefs.getInt(KEY_USER_ID_FOR_APP, -1);
        }

        if (currentUserId == -1) {
            Toast.makeText(this, "사용자 정보가 없습니다. 로그인 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "User ID is not available.");
            finish();
            return;
        }

        tvRecommendationContent = findViewById(R.id.tvAllRoutine);
        btnApplyRoutine = findViewById(R.id.btnApplyRoutine);
        btnRetryRoutine = findViewById(R.id.btnRetryRoutine);

        if (ApiClient.getClient() != null) {
            aiRoutineApiService = ApiClient.getClient().create(AiRoutineApi.class);
            routineApiService = ApiClient.getClient().create(RoutineApi.class);
            spaceApiService = ApiClient.getClient().create(SpaceApi.class);
        } else {
            Log.e(TAG, "ApiClient.getClient() is null. Check Retrofit setup.");
            Toast.makeText(this, "네트워크 설정 오류입니다. 앱을 재시작해주세요.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fetchUserSpacesAndThenAiRecommendations(currentUserId);

        btnApplyRoutine.setOnClickListener(v -> {
            if (!currentRecommendations.isEmpty()) {
                showRoutineApplyOptions(currentRecommendations.get(0));
            } else {
                Toast.makeText(RoutineAllActivity.this, "반영할 추천 루틴이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnRetryRoutine.setOnClickListener(v -> {
            if (currentUserId != -1) {
                fetchUserSpacesAndThenAiRecommendations(currentUserId);
            }
        });
    }

    private void fetchUserSpacesAndThenAiRecommendations(int userId) {
        Log.d(TAG, "사용자 공간 목록 요청 시작, userId: " + userId);
        tvRecommendationContent.setText("사용자님의 공간 정보를 불러오는 중입니다...");

        spaceApiService.getSpacesByUserId(userId).enqueue(new Callback<List<Space>>() {
            @Override
            public void onResponse(@NonNull Call<List<Space>> call, @NonNull Response<List<Space>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userSpaceList.clear();
                    userSpaceList.addAll(response.body());
                } else {
                    handleApiError(response, "사용자 공간 목록 로드 실패. 일반 추천을 시도합니다.");
                    userSpaceList.clear();
                }
                fetchAiAllRecommendations(userId);
            }

            @Override
            public void onFailure(@NonNull Call<List<Space>> call, @NonNull Throwable t) {
                handleApiFailure(t, "사용자 공간 목록 API 호출 오류. 일반 추천을 시도합니다.");
                userSpaceList.clear();
                fetchAiAllRecommendations(userId);
            }
        });
    }

    private void fetchAiAllRecommendations(int userId) {
        RecommendationRequest request = new RecommendationRequest(userId, null);
        tvRecommendationContent.setText("AI가 전체 공간에 대한 맞춤 루틴을 생성 중입니다...\n잠시만 기다려 주세요. 🤔");
        currentRecommendations.clear();

        aiRoutineApiService.generateAiRoutineRecommendations(request).enqueue(new Callback<List<Recommendation>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recommendation>> call, @NonNull Response<List<Recommendation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentRecommendations.addAll(response.body());
                    if (currentRecommendations.isEmpty()) {
                        tvRecommendationContent.setText("생성된 추천 루틴이 없습니다. 😥");
                    } else {
                        displayRecommendationsAsText(currentRecommendations);
                        Toast.makeText(RoutineAllActivity.this, currentRecommendations.size() + "개의 AI 추천을 받았습니다!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    handleApiError(response, "AI 전체 추천 생성 실패");
                    tvRecommendationContent.setText("추천을 가져오는 데 실패했습니다. 😭");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Recommendation>> call, @NonNull Throwable t) {
                handleApiFailure(t, "AI 전체 추천 생성 API 호출 오류");
                tvRecommendationContent.setText("네트워크 오류로 AI 추천을 가져올 수 없습니다. 📡");
            }
        });
    }

    private void displayRecommendationsAsText(List<Recommendation> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            tvRecommendationContent.setText("표시할 추천 내용이 없습니다.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recommendations.size(); i++) {
            Recommendation rec = recommendations.get(i);
            sb.append("✨ 추천 ").append(i + 1).append(" ✨\n");
            if (rec.getTitle() != null) sb.append("📌 제목: ").append(rec.getTitle()).append("\n");
            if (rec.getDescription() != null) sb.append("📝 설명: ").append(rec.getDescription()).append("\n");
            if (rec.getSpace_id() != null && rec.getSpace_id() > 0) {
                String spaceName = getSpaceNameById(rec.getSpace_id());
                sb.append("📍 관련 공간: ").append(spaceName).append("\n");
            }
            sb.append("\n");
        }

        tvRecommendationContent.setText(sb.toString());
    }

    private String getSpaceNameById(int spaceIdToFind) {
        for (Space space : userSpaceList) {
            if (space.getSpace_id() == spaceIdToFind) {
                return space.getName();
            }
        }
        return "지정 공간 (ID: " + spaceIdToFind + ")";
    }

    private void showRoutineApplyOptions(Recommendation recommendationToApply) {
        Log.d(TAG, "루틴 반영 시도: " + recommendationToApply.getTitle());
        CleaningRoutine routineToSave = new CleaningRoutine();
        routineToSave.setUser_id(currentUserId);
        // 나머지 설정은 사용자 입력 UI 이후 구현
    }

    private void handleApiError(Response<?> response, String logMessage) {
        Log.e(TAG, logMessage + " | 응답 코드: " + response.code());
    }

    private void handleApiFailure(Throwable t, String logMessage) {
        Log.e(TAG, logMessage, t);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}