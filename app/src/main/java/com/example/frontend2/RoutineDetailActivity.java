package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.frontend2.api.AiRoutineApi;
import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.RoutineApi;
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.Recommendation;
import com.example.frontend2.models.RecommendationRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutineDetailActivity extends AppCompatActivity {

    private static final String TAG = "RoutineDetailActivity";
    private AiRoutineApi aiRoutineApiService;
    private RoutineApi routineApiService;

    private TextView tvRoomNameLabel;
    private TextView tvRecommendationContent;
    private Button btnApplyRoutine;
    private Button btnRetryRoutine;
    private String screenTitleTextFromIntent;
    private List<Recommendation> currentRecommendations = new ArrayList<>();
    private int currentUserId = -1;
    private String currentRoomName;
    private int currentSpaceId = -1;

    public static final String PREFS_NAME_FOR_APP = "CleanItAppPrefs";
    public static final String KEY_USER_ID_FOR_APP = "logged_in_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_detail);

        Intent intent = getIntent();
        if (intent != null) {
            currentUserId = intent.getIntExtra("userId", -1);
            screenTitleTextFromIntent = intent.getStringExtra("roomName"); // 예: "전체 공간", "욕실"
            currentRoomName = intent.getStringExtra("roomName"); // 예: "욕실"
            currentSpaceId = intent.getIntExtra("spaceId", -1);  // 예: 2 (욕실 ID)

            if (currentUserId != -1) {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME_FOR_APP, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(KEY_USER_ID_FOR_APP, currentUserId);
                editor.apply();
                Log.d(TAG, "userId를 SharedPreferences에 저장 완료: " + currentUserId);
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(currentRoomName != null ? currentRoomName + " AI 추천" : "AI 공간별 추천");
        }

        if (currentUserId == -1 || currentSpaceId <= 0 || currentRoomName == null || currentRoomName.isEmpty()) {
            Toast.makeText(this, "추천을 위한 정보(사용자 또는 특정 공간)가 올바르지 않습니다.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Required data (userId, valid spaceId, or roomName) is missing or invalid. UserId: " + currentUserId + ", SpaceId: " + currentSpaceId + ", RoomName: " + currentRoomName);
            finish();
            return;
        }

        tvRoomNameLabel = findViewById(R.id.tvRoomName);
        tvRecommendationContent = findViewById(R.id.tvRecommendations);
        btnApplyRoutine = findViewById(R.id.btnApplyRoutine);
        btnRetryRoutine = findViewById(R.id.btnRetryRoutine);

        tvRoomNameLabel.setText(currentRoomName + " 맞춤 루틴");

        if (ApiClient.getClient() != null) {
            aiRoutineApiService = ApiClient.getClient().create(AiRoutineApi.class);
            routineApiService = ApiClient.getClient().create(RoutineApi.class);
        } else {
            Log.e(TAG, "ApiClient.getClient() is null. Check Retrofit setup.");
            Toast.makeText(this, "네트워크 설정 오류입니다. 앱을 재시작해주세요.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fetchAiRecommendationsForSpecificSpace(currentUserId, currentSpaceId);

        btnApplyRoutine.setOnClickListener(v -> {
            if (!currentRecommendations.isEmpty()) {
                showApplyRoutineDialog(currentRecommendations.get(0));
            } else {
                Toast.makeText(this, "반영할 추천이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        btnRetryRoutine.setOnClickListener(v -> fetchAiRecommendationsForSpecificSpace(currentUserId, currentSpaceId));
    }

    private void fetchAiRecommendationsForSpecificSpace(int userId, int spaceId) {
        RecommendationRequest request = new RecommendationRequest(userId, spaceId);
        Log.d(TAG, "AI 특정 공간 추천 요청: userId=" + userId + ", spaceId=" + spaceId);
        tvRecommendationContent.setText("AI가 '" + currentRoomName + "' 공간을 위한 맞춤 루틴을 생성 중입니다...");
        currentRecommendations.clear();

        aiRoutineApiService.generateAiRoutineRecommendations(request).enqueue(new Callback<List<Recommendation>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recommendation>> call, @NonNull Response<List<Recommendation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Recommendation> recommendations = response.body();
                    currentRecommendations.addAll(recommendations);
                    if (recommendations.isEmpty()) {
                        tvRecommendationContent.setText("'" + currentRoomName + "' 공간에 대한 추천 루틴이 생성되지 않았습니다. 😥");
                    } else {
                        displayRecommendationsAsText(recommendations);
                        Toast.makeText(RoutineDetailActivity.this, recommendations.size() + "개의 AI 추천을 받았습니다!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    handleApiError(response, "AI 특정 공간 추천 생성 실패");
                    tvRecommendationContent.setText("'" + currentRoomName + "' 공간 추천을 가져오는 데 실패했습니다. 😭");
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Recommendation>> call, @NonNull Throwable t) {
                handleApiFailure(t, "AI 특정 공간 추천 API 호출 오류");
                tvRecommendationContent.setText("네트워크 오류로 '" + currentRoomName + "' 공간 추천을 가져올 수 없습니다. 📡");
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
            // 이 화면은 특정 공간에 대한 것이므로, "관련 공간" 정보는 currentRoomName을 사용하거나 생략 가능
            // sb.append("📍 공간: ").append(currentRoomName).append("\n"); // 필요시 추가
            sb.append("\n");
        }
        tvRecommendationContent.setText(sb.toString());
    }

    private void showApplyRoutineDialog(Recommendation recommendationToApply) {
        Log.d(TAG, "루틴 반영 시도: " + recommendationToApply.getTitle() + " (공간: " + currentRoomName + ")");
        CleaningRoutine routineToSave = new CleaningRoutine();
        routineToSave.setUser_id(currentUserId);
        routineToSave.setSpace_id(currentSpaceId); // 이 화면은 특정 공간이므로 currentSpaceId 사용
        routineToSave.setTitle(recommendationToApply.getTitle());
        routineToSave.setDescription(recommendationToApply.getDescription());
        // TODO: 사용자가 반복 주기(repeat_unit, repeat_interval) 등을 설정할 UI 필요
        routineToSave.setRepeat_unit("NONE"); // 또는 적절한 기본값
        routineToSave.setRepeat_interval(null);

        saveRoutineToBackend(routineToSave);
    }

    private void saveRoutineToBackend(CleaningRoutine routineToSave) {
        // ... (RoutineAllActivity와 동일한 내용)
        if (routineApiService == null) { Toast.makeText(this, "네트워크 서비스 오류", Toast.LENGTH_SHORT).show(); return; }
        Log.d(TAG, "백엔드에 루틴 저장 요청: " + routineToSave.getTitle() + " for spaceId: " + routineToSave.getSpace_id());
        routineApiService.createRoutine(routineToSave).enqueue(new Callback<CleaningRoutine>() {
            @Override
            public void onResponse(@NonNull Call<CleaningRoutine> call, @NonNull Response<CleaningRoutine> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RoutineDetailActivity.this, "'" + response.body().getTitle() + "' 루틴 반영됨!", Toast.LENGTH_LONG).show();
                } else { handleApiError(response, "루틴 반영 실패"); }
            }
            @Override
            public void onFailure(@NonNull Call<CleaningRoutine> call, @NonNull Throwable t) {
                handleApiFailure(t, "루틴 반영 API 호출 오류");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleApiError(Response<?> response, String defaultMessage) {
        String errorMessage = defaultMessage + " (코드: " + response.code() + ")";
        if (response.errorBody() != null) {
            try {
                errorMessage += "\n내용: " + response.errorBody().string();
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