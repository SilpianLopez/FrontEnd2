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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

// ★ 실제 프로젝트의 경로로 모두 수정하세요.
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.Recommendation;
import com.example.frontend2.models.RecommendationRequest;
import com.example.frontend2.models.Space;
import com.example.frontend2.models.RoutineRequest; // 루틴 생성 DTO
import com.example.frontend2.api.AiRoutineApi;
import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.RoutineApi;   // 일반 루틴 생성 API 인터페이스
import com.example.frontend2.api.SpaceApi;

import java.io.IOException;
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
    // ActivityResultLauncher는 이 Activity에서 직접 사용하지 않음 (바로 저장하고 끝)

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
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(screenTitleTextFromIntent != null && !screenTitleTextFromIntent.isEmpty() ? screenTitleTextFromIntent + " AI 추천" : "AI 전체 루틴 추천");
        }

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
        Log.d(TAG, "onCreate: Current User ID: " + currentUserId);

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
                // ❗️ 사용자 선택 없이 모든 추천을 기본값으로 즉시 저장
                applyAllAiRecommendationsWithDefaults(currentRecommendations);
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
                    Log.d(TAG, "사용자 공간 목록 로드 성공: " + userSpaceList.size() + "개");
                    for (Space space : userSpaceList) {
                        Log.d(TAG, "로드된 공간: ID=" + space.getSpace_id() + ", 이름=" + space.getName());
                    }
                } else {
                    handleApiError(response, "사용자 공간 목록 로드 실패. 일반 추천을 시도합니다.");
                    Log.w(TAG, "Failed to load user spaces or no spaces found (userId: " + userId + "). Proceeding with general AI recommendation.");
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
        Log.d(TAG, "AI 전체 추천 요청: userId=" + userId + ", spaceId=null");
        tvRecommendationContent.setText("AI가 전체 공간에 대한 맞춤 루틴을 생성 중입니다...\n잠시만 기다려 주세요. 🤔");
        currentRecommendations.clear();

        aiRoutineApiService.generateAiRoutineRecommendations(request).enqueue(new Callback<List<Recommendation>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recommendation>> call, @NonNull Response<List<Recommendation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentRecommendations.clear();
                    currentRecommendations.addAll(response.body());
                    if (currentRecommendations.isEmpty()) {
                        tvRecommendationContent.setText("생성된 추천 루틴이 없습니다. 😥");
                    } else {
                        displayRecommendationsAsText(currentRecommendations);
                        Toast.makeText(RoutineAllActivity.this, currentRecommendations.size() + "개의 AI 추천을 받았습니다!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    handleApiError(response, "AI 전체 추천 생성 실패");
                    tvRecommendationContent.setText("AI 추천을 가져오는 데 실패했습니다. 😭 (응답 코드: " + response.code() +")");
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
        if (userSpaceList != null) {
            for (Space space : userSpaceList) {
                if (space.getSpace_id() == spaceIdToFind) {
                    return space.getName();
                }
            }
        }
        return "지정 공간 (ID: " + spaceIdToFind + ")";
    }

    /**
     * AI가 추천한 모든 루틴을 기본 설정("반복 안함")으로 사용자의 실제 루틴으로 저장합니다.
     */
    private void applyAllAiRecommendationsWithDefaults(List<Recommendation> recommendationsToApply) {
        if (recommendationsToApply == null || recommendationsToApply.isEmpty()) {
            Toast.makeText(this, "반영할 추천 내용이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "모든 AI 추천 루틴 즉시 반영 시도: " + recommendationsToApply.size() + "개");

        List<RoutineRequest> routinesToSaveRequests = new ArrayList<>();

        for (Recommendation rec : recommendationsToApply) {
            int finalSpaceId = determineSpaceIdForRoutineSave(rec.getSpace_id());

            if (finalSpaceId == -1 && !(userSpaceList == null || userSpaceList.isEmpty())) {
                Log.w(TAG, "'" + rec.getTitle() + "' 추천을 저장할 공간을 결정할 수 없습니다. 건너뜁니다.");
                continue;
            }
            if (finalSpaceId == -1 && (userSpaceList == null || userSpaceList.isEmpty())){
                Log.w(TAG, "'" + rec.getTitle() + "' 추천을 저장할 사용자 공간이 없습니다. 건너뜁니다.");
                continue;
            }

            RoutineRequest routineRequest = new RoutineRequest(
                    finalSpaceId,
                    currentUserId,
                    rec.getTitle(),
                    rec.getDescription(),
                    "NONE", // 반복 단위: 기본값 "반복 안함"
                    null,   // 반복 간격: 반복 안하므로 null
                    null    // first_due_date: null (백엔드에서 오늘 기준으로 next_due_date 계산)
            );
            routinesToSaveRequests.add(routineRequest);
        }

        if (routinesToSaveRequests.isEmpty()) {
            Toast.makeText(this, "저장할 유효한 추천 루틴이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        saveMultipleRoutinesToBackend(routinesToSaveRequests);
    }

    /**
     * AI 추천 객체와 사용자 공간 목록을 바탕으로 루틴을 저장할 최종 space_id를 결정합니다.
     */
    private int determineSpaceIdForRoutineSave(Integer recommendedSpaceId) {
        if (recommendedSpaceId != null && recommendedSpaceId > 0) {
            if (userSpaceList != null) {
                for (Space space : userSpaceList) {
                    if (space.getSpace_id() == recommendedSpaceId) {
                        return recommendedSpaceId;
                    }
                }
            }
            Log.w(TAG, "AI 추천의 space_id(" + recommendedSpaceId + ")가 사용자 공간 목록에 없습니다.");
        }
        // AI 추천에 space_id가 없거나 유효하지 않고, 사용자 공간 목록이 있다면 첫 번째 공간 사용
        if (userSpaceList != null && !userSpaceList.isEmpty()) {
            Log.d(TAG, "AI 추천에 특정 공간이 없어 첫 번째 사용자 공간 ("+ userSpaceList.get(0).getName() +")을 사용합니다.");
            return userSpaceList.get(0).getSpace_id();
        }
        Log.e(TAG, "루틴을 저장할 유효한 공간 ID를 결정할 수 없습니다.");
        return -1; // 유효한 공간 ID를 찾지 못함
    }

    /**
     * 여러 개의 RoutineRequest 객체를 백엔드에 순차적으로 저장하는 API를 호출합니다.
     */
    private void saveMultipleRoutinesToBackend(List<RoutineRequest> routineRequestsToSave) {
        if (routineApiService == null) {
            Toast.makeText(this, "네트워크 서비스 오류입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (routineRequestsToSave.isEmpty()) {
            Log.d(TAG, "저장할 루틴이 없습니다.");
            return;
        }

        Log.d(TAG, "백엔드에 " + routineRequestsToSave.size() + "개의 루틴 저장 요청 시도...");
        Toast.makeText(this, routineRequestsToSave.size() + "개의 루틴을 반영 중입니다...", Toast.LENGTH_SHORT).show();

        final int[] successCount = {0};
        final int[] failCount = {0};
        final int totalRequests = routineRequestsToSave.size();

        for (RoutineRequest request : routineRequestsToSave) {
            Call<CleaningRoutine> call = routineApiService.createRoutine(request);
            call.enqueue(new Callback<CleaningRoutine>() {
                @Override
                public void onResponse(@NonNull Call<CleaningRoutine> call, @NonNull Response<CleaningRoutine> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        successCount[0]++;
                        Log.d(TAG, "'" + response.body().getTitle() + "' 루틴 반영 성공!");
                    } else {
                        failCount[0]++;
                        Log.e(TAG, "'" + request.getTitle() + "' 루틴 반영 실패 (코드: " + response.code() + ")");
                    }
                    checkAllRoutinesApplied(totalRequests, successCount[0], failCount[0]);
                }

                @Override
                public void onFailure(@NonNull Call<CleaningRoutine> call, @NonNull Throwable t) {
                    failCount[0]++;
                    Log.e(TAG, "'" + request.getTitle() + "' 루틴 반영 API 호출 오류", t);
                    checkAllRoutinesApplied(totalRequests, successCount[0], failCount[0]);
                }
            });
        }
    }

    private void checkAllRoutinesApplied(int total, int success, int fail) {
        if (success + fail == total) { // 모든 요청이 완료됨
            if (success == total) {
                Toast.makeText(RoutineAllActivity.this, "모든 AI 추천 루틴이 성공적으로 반영되었습니다! 필요시 목록에서 수정해주세요.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(RoutineAllActivity.this, success + "개 루틴 반영 성공, " + fail + "개 실패했습니다.", Toast.LENGTH_LONG).show();
            }
            // 성공/실패 여부와 관계없이 이전 화면으로 돌아가도록 결과 설정 및 종료
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent); // 이전 화면에서 onActivityResult로 받을 수 있도록
            finish(); // 현재 AI 추천 화면 종료
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // 공통 에러 처리 헬퍼 함수 (이전과 동일)
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