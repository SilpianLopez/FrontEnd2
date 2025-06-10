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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.AiRoutineApi;
import com.example.frontend2.api.ApiClient;
// RoutineApi는 CleaningAdd_UI에서 사용하므로 여기서는 직접 필요 없을 수 있음
import com.example.frontend2.models.Recommendation;
import com.example.frontend2.models.RecommendationRequest;
// Space 모델은 이 Activity에서 공간 목록을 직접 가져오지 않으므로 제거 가능

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutineDetailActivity extends AppCompatActivity {

    private static final String TAG = "RoutineDetailActivity";
    private AiRoutineApi aiRoutineApiService;
    // private RoutineApi routineApiService; // CleaningAdd_UI에서 사용

    private TextView tvRoomNameLabel;
    private TextView tvRecommendationContent;
    private Button btnApplyRoutine;
    private Button btnRetryRoutine;

    private List<Recommendation> currentRecommendations = new ArrayList<>();
    private int currentUserId = -1;
    private String currentRoomName;
    private int currentSpaceId = -1;

    public static final String PREFS_NAME_FOR_APP = "CleanItAppPrefs";
    public static final String KEY_USER_ID_FOR_APP = "logged_in_user_id";
    private ActivityResultLauncher<Intent> addRoutineLauncherDetail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_detail);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME_FOR_APP, Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt(KEY_USER_ID_FOR_APP, -1);

        Intent intent = getIntent();
        if (intent != null) {
            // currentUserId = intent.getIntExtra("userId", -1);
            currentRoomName = intent.getStringExtra("roomName");
            currentSpaceId = intent.getIntExtra("spaceId", -1);
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
        Log.d(TAG, "onCreate: UserId=" + currentUserId + ", SpaceId=" + currentSpaceId + ", RoomName=" + currentRoomName);

        tvRoomNameLabel = findViewById(R.id.tvRoomName);
        tvRecommendationContent = findViewById(R.id.tvRecommendations);
        btnApplyRoutine = findViewById(R.id.btnApplyRoutine);
        btnRetryRoutine = findViewById(R.id.btnRetryRoutine);

        tvRoomNameLabel.setText(currentRoomName + " 맞춤 루틴");

        if (ApiClient.getClient() != null) {
            aiRoutineApiService = ApiClient.getClient().create(AiRoutineApi.class);
            // routineApiService는 CleaningAdd_UI에서 사용
        } else {
            Log.e(TAG, "ApiClient.getClient() is null. Check Retrofit setup.");
            Toast.makeText(this, "네트워크 설정 오류입니다. 앱을 재시작해주세요.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        addRoutineLauncherDetail = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Toast.makeText(this, "AI 추천 루틴이 성공적으로 반영되었습니다!", Toast.LENGTH_LONG).show();
                        // TODO: UI 업데이트 또는 화면 전환
                    } else {
                        Log.d(TAG, "루틴 반영이 취소되었거나 실패했습니다.");
                    }
                });

        fetchAiRecommendationsForSpecificSpace(currentUserId, currentSpaceId);

        btnApplyRoutine.setOnClickListener(v -> {
            if (!currentRecommendations.isEmpty()) {
                selectRecommendationToForwardToAddScreen(currentRecommendations);
            } else {
                Toast.makeText(RoutineDetailActivity.this, "반영할 추천이 없습니다.", Toast.LENGTH_SHORT).show();
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
                    currentRecommendations.clear();
                    currentRecommendations.addAll(response.body());
                    if (currentRecommendations.isEmpty()) {
                        tvRecommendationContent.setText("'" + currentRoomName + "' 공간에 대한 추천 루틴이 생성되지 않았습니다. 😥");
                    } else {
                        displayRecommendationsAsText(currentRecommendations);
                        Toast.makeText(RoutineDetailActivity.this, currentRecommendations.size() + "개의 AI 추천을 받았습니다!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    handleApiError(response, "AI '" + currentRoomName + "' 공간 추천 생성 실패");
                    tvRecommendationContent.setText("'" + currentRoomName + "' 공간 추천을 가져오는 데 실패했습니다. 😭");
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Recommendation>> call, @NonNull Throwable t) {
                handleApiFailure(t, "AI '" + currentRoomName + "' 공간 추천 API 호출 오류");
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
            // 이 화면은 특정 공간에 대한 것이므로, "관련 공간" 정보는 이미 currentRoomName으로 인지하고 있음.
            // 필요하다면 sb.append("📍 이 공간(" + currentRoomName + ")에 대한 추천입니다.\n");
            sb.append("\n");
        }
        tvRecommendationContent.setText(sb.toString());
    }

    private void selectRecommendationToForwardToAddScreen(List<Recommendation> recommendations) {
        // ... (RoutineAllActivity의 selectRecommendationToForwardToAddScreen와 거의 동일한 로직,
        //      단, AlertDialog 제목 등에 currentRoomName 활용 가능)
        if (recommendations == null || recommendations.isEmpty()) { /* ... */ return; }
        if (recommendations.size() > 1) {
            CharSequence[] recommendationTitles = new CharSequence[recommendations.size()];
            for (int i = 0; i < recommendations.size(); i++) {
                recommendationTitles[i] = (i + 1) + ". " + recommendations.get(i).getTitle();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("'" + currentRoomName + "' 공간에 반영할 추천 선택");
            builder.setItems(recommendationTitles, (dialog, which) -> {
                startCleaningAddActivityWithData(recommendations.get(which));
            });
            builder.setNegativeButton("취소", null);
            builder.show();
        } else if (recommendations.size() == 1) {
            startCleaningAddActivityWithData(recommendations.get(0));
        }
    }

    private void startCleaningAddActivityWithData(Recommendation recommendationToApply) {
        Intent intent = new Intent(this, CleaningAdd_UI.class);
        intent.putExtra("userId", currentUserId);
        intent.putExtra("suggestedTitle", recommendationToApply.getTitle());
        intent.putExtra("suggestedDescription", recommendationToApply.getDescription());
        // 이 화면은 특정 공간에 대한 것이므로, AI 추천 객체의 space_id 대신 currentSpaceId를 사용하거나,
        // 일관성을 위해 recommendationToApply.getSpace_id() (이 값은 currentSpaceId와 같아야 함)를 전달.
        intent.putExtra("preselected_space_id", currentSpaceId);
        intent.putExtra("preselected_space_name", currentRoomName);
        intent.putExtra("isFromAiRecommendation", true);
        addRoutineLauncherDetail.launch(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 공통 에러 처리 헬퍼 함수들
    private void handleApiError(Response<?> response, String defaultMessage) {

        String errorMessage = defaultMessage + " (코드: " + response.code() + ")";
        if (response.errorBody() != null) {
            try {
                errorMessage += "\n내용: " + response.errorBody().string();
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
}