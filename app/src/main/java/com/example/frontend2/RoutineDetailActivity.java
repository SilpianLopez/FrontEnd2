package com.example.frontend2; // ★ 실제 프로젝트의 패키지 경로로 수정하세요.

import android.content.Intent;
import android.content.SharedPreferences; // SharedPreferences 사용 예시 (선택 사항)
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // AlertDialog 사용 예시
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

// ★ 실제 프로젝트의 경로로 모두 수정하세요.
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.Recommendation;
import com.example.frontend2.models.RecommendationRequest;
import com.example.frontend2.api.AiRoutineApi;
import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.RoutineApi;
// 이 Activity에서는 Space 목록을 직접 가져올 필요는 없을 수 있습니다.

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutineDetailActivity extends AppCompatActivity {

    private static final String TAG = "RoutineDetailActivity";
    private AiRoutineApi aiRoutineApiService;
    private RoutineApi routineApiService; // 루틴 반영용

    private TextView tvRoomNameLabel; // XML ID: tvRoomName (화면 상단 공간 이름 표시용)
    private TextView tvRecommendationContent; // XML ID: tvRecommendations (추천 내용 표시용)
    private Button btnApplyRoutine;
    private Button btnRetryRoutine;

    private List<Recommendation> currentRecommendations = new ArrayList<>();
    private int currentUserId = -1;
    private String currentRoomName; // Intent로 전달받은 현재 공간 이름
    private int currentSpaceId = -1;  // Intent로 전달받은 현재 공간 ID (유효한 ID여야 함)

    public static final String PREFS_NAME_FOR_APP = "CleanItAppPrefs"; // SharedPreferences 이름
    public static final String KEY_USER_ID_FOR_APP = "logged_in_user_id"; // 사용자 ID 키


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 이 Activity는 activity_routine_detail.xml 레이아웃을 사용해야 합니다.
        setContentView(R.layout.activity_routine_detail);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        // Intent로부터 필수 데이터 가져오기
        Intent intent = getIntent();
        if (intent != null) {
            currentUserId = intent.getIntExtra("userId", -1);
            currentRoomName = intent.getStringExtra("roomName");
            currentSpaceId = intent.getIntExtra("spaceId", -1); // 0이나 음수는 유효하지 않은 ID로 간주
        }

        // Toolbar 제목 및 뒤로가기 버튼 설정
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(currentRoomName != null ? currentRoomName + " AI 추천" : "AI 공간별 추천");
        }

        // 필수 데이터 유효성 검사
        if (currentUserId == -1 || currentSpaceId <= 0 || currentRoomName == null || currentRoomName.isEmpty()) {
            Toast.makeText(this, "추천을 위한 정보(사용자 또는 특정 공간)가 올바르지 않습니다.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Required data (userId, valid spaceId, or roomName) is missing or invalid. UserId: " + currentUserId + ", SpaceId: " + currentSpaceId + ", RoomName: " + currentRoomName);
            finish(); // 필요한 정보 없으면 Activity 종료
            return;
        }

        // UI 요소 초기화
        tvRoomNameLabel = findViewById(R.id.tvRoomName); // XML에 정의된 ID
        tvRecommendationContent = findViewById(R.id.tvRecommendations); // XML에 정의된 ID
        btnApplyRoutine = findViewById(R.id.btnApplyRoutine);
        btnRetryRoutine = findViewById(R.id.btnRetryRoutine);

        tvRoomNameLabel.setText(currentRoomName + " 맞춤 루틴"); // 화면 상단에 현재 공간 이름 표시

        // Retrofit 서비스 초기화
        if (ApiClient.getClient() != null) {
            aiRoutineApiService = ApiClient.getClient().create(AiRoutineApi.class);
            routineApiService = ApiClient.getClient().create(RoutineApi.class);
        } else {
            Log.e(TAG, "ApiClient.getClient() is null. Check Retrofit setup.");
            Toast.makeText(this, "네트워크 설정 오류입니다. 앱을 재시작해주세요.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 특정 공간에 대한 AI 추천 요청
        fetchAiRecommendationsForSpecificSpace(currentUserId, currentSpaceId);

        btnApplyRoutine.setOnClickListener(v -> {
            if (!currentRecommendations.isEmpty()) {
                // TODO: 사용자가 여러 추천 중 하나를 선택하는 UI가 있다면 그 선택된 추천을 사용.
                //       현재는 첫 번째 추천을 대상으로 함.
                showApplyRoutineDialog(currentRecommendations.get(0));
            } else {
                Toast.makeText(RoutineDetailActivity.this, "반영할 추천 루틴이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnRetryRoutine.setOnClickListener(v -> {
            if (currentUserId != -1 && currentSpaceId > 0) { // 유효한 ID일 때만 다시 요청
                fetchAiRecommendationsForSpecificSpace(currentUserId, currentSpaceId);
            }
        });
    }

    /**
     * 현재 Activity의 특정 공간에 대한 AI 추천을 요청합니다.
     */
    private void fetchAiRecommendationsForSpecificSpace(int userId, int spaceId) {
        RecommendationRequest request = new RecommendationRequest(userId, spaceId); // spaceId 명시
        Log.d(TAG, "AI 특정 공간 추천 요청: userId=" + userId + ", spaceId=" + spaceId + ", roomName=" + currentRoomName);
        tvRecommendationContent.setText("AI가 '" + currentRoomName + "' 공간을 위한 맞춤 루틴을 생성 중입니다...\n잠시만 기다려 주세요. 🤔");
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

    /**
     * 받은 추천 목록을 하나의 문자열로 만들어 TextView에 표시합니다.
     * 이 화면은 특정 공간에 대한 것이므로, "관련 공간" 정보는 생략하거나 currentRoomName을 활용.
     * 타입 정보는 표시하지 않습니다.
     */
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
            // 이 화면은 이미 특정 공간에 대한 추천이므로, rec.getSpace_id()를 다시 표시할 필요는 없을 수 있습니다.
            // 필요하다면, 일관성을 위해 sb.append("📍 공간: ").append(currentRoomName).append("\n"); 와 같이 표시 가능
            sb.append("\n"); // 각 추천 사이에 공백
        }
        tvRecommendationContent.setText(sb.toString());
    }

    /**
     * 선택된 AI 추천을 사용자의 실제 루틴으로 저장하기 위한 UI(예: 다이얼로그)를 표시하거나
     * 바로 저장 로직을 호출합니다.
     */
    private void showApplyRoutineDialog(Recommendation recommendationToApply) {
        Log.d(TAG, "루틴 반영 시도: " + recommendationToApply.getTitle() + " (공간: " + currentRoomName + ")");

        // TODO: AlertDialog 또는 별도의 Activity/Fragment를 사용하여 사용자로부터
        //       반복 단위(repeat_unit), 반복 간격(repeat_interval), 첫 시작일 등을 입력받습니다.
        //       AI 추천의 title과 description은 기본값으로 채워줄 수 있습니다.

        // --- 임시 로직: 다이얼로그 없이 바로 저장 시도 (기본값 사용) ---
        CleaningRoutine routineToSave = new CleaningRoutine();
        routineToSave.setUser_id(currentUserId);
        routineToSave.setSpace_id(currentSpaceId); // 이 화면은 특정 공간이므로 currentSpaceId 사용
        routineToSave.setTitle(recommendationToApply.getTitle());
        routineToSave.setDescription(recommendationToApply.getDescription());

        // 사용자가 설정할 수 있도록 UI 제공 필요 (아래는 임시 기본값)
        routineToSave.setRepeat_unit("NONE"); // 예: 특정 공간 추천은 일회성 작업일 수 있음
        routineToSave.setRepeat_interval(null);
        // routineToSave.setFirst_due_date(null); // 백엔드에서 처리하도록 설정 가능

        saveRoutineToBackend(routineToSave);
        // --- 임시 로직 끝 ---
    }

    private void saveRoutineToBackend(CleaningRoutine routineToSave) {
        if (routineApiService == null) {
            Toast.makeText(this, "네트워크 서비스 오류 (루틴 저장)", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "백엔드에 루틴 저장 요청: " + routineToSave.getTitle() + " for spaceId: " + routineToSave.getSpace_id());
        routineApiService.createRoutine(routineToSave).enqueue(new Callback<CleaningRoutine>() {
            @Override
            public void onResponse(@NonNull Call<CleaningRoutine> call, @NonNull Response<CleaningRoutine> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CleaningRoutine savedRoutine = response.body();
                    Log.d(TAG, "루틴 반영(저장) 성공! 새 루틴 ID: " + savedRoutine.getRoutine_id());
                    Toast.makeText(RoutineDetailActivity.this, "'" + savedRoutine.getTitle() + "' 루틴이 성공적으로 반영되었습니다!", Toast.LENGTH_LONG).show();
                    // TODO: 성공 후 UI 업데이트 (예: 이전 화면으로 돌아가거나, 루틴 목록 새로고침)
                    //       setResult(RESULT_OK); finish(); 등을 사용하여 이전 화면에 알릴 수 있음
                } else {
                    handleApiError(response, "루틴 반영(저장) 실패");
                }
            }
            @Override
            public void onFailure(@NonNull Call<CleaningRoutine> call, @NonNull Throwable t) {
                handleApiFailure(t, "루틴 반영(저장) API 호출 오류");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 뒤로가기 버튼 클릭 시 현재 Activity 종료
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