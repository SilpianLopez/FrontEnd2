package com.example.frontend2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.frontend2.api.AiRoutineApi;
import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.RoutineApi;
import com.example.frontend2.api.SpaceApi;
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.Recommendation;
import com.example.frontend2.models.RecommendationRequest;
import com.example.frontend2.models.Space;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    public static final int REQUEST_CODE_APPLY_ROUTINE = 1001; // 루틴 반영 후 결과 코드

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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 활성화
            getSupportActionBar().setTitle(screenTitleTextFromIntent != null && !screenTitleTextFromIntent.isEmpty() ? screenTitleTextFromIntent + " AI 추천" : "AI 전체 루틴 추천");
        }

        if (currentUserId == -1) { // Intent에 없으면 SharedPreferences에서 시도
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

        // 1. 사용자 공간 목록 먼저 로드 -> 성공 시 AI 전체 추천 요청
        fetchUserSpacesAndThenAiRecommendations(currentUserId);

        btnApplyRoutine.setOnClickListener(v -> {
            if (!currentRecommendations.isEmpty()) {
                // TODO: 사용자가 여러 추천 중 하나를 선택하는 UI가 필요 (예: AlertDialog로 목록 보여주기)
                // 현재는 첫 번째 추천을 대상으로 함
                showRoutineApplyOptionsDialog(currentRecommendations.get(0));
            } else {
                Toast.makeText(RoutineAllActivity.this, "반영할 추천 루틴이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnRetryRoutine.setOnClickListener(v -> {
            if (currentUserId != -1) {
                // "다시 추천" 시, 공간 목록부터 새로고침 후 AI 추천 요청
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
                } else {
                    handleApiError(response, "사용자 공간 목록 로드 실패. 일반 추천을 시도합니다.");
                    Log.w(TAG, "Failed to load user spaces or no spaces found (userId: " + userId + "). Proceeding with general AI recommendation.");
                    userSpaceList.clear(); // 실패 시 목록 비움
                }
                // 공간 목록 로드 성공/실패 여부와 관계없이 AI 전체 추천 요청
                fetchAiAllRecommendations(userId);
            }

            @Override
            public void onFailure(@NonNull Call<List<Space>> call, @NonNull Throwable t) {
                handleApiFailure(t, "사용자 공간 목록 API 호출 오류. 일반 추천을 시도합니다.");
                userSpaceList.clear(); // 네트워크 오류 시 목록 비움
                fetchAiAllRecommendations(userId); // AI 추천은 시도
            }
        });
    }

    private void fetchAiAllRecommendations(int userId) {
        // 이 Activity는 "전체 추천"이므로 spaceId는 항상 null
        RecommendationRequest request = new RecommendationRequest(userId, null);

        Log.d(TAG, "AI 전체 추천 요청: userId=" + userId + ", spaceId=null");
        tvRecommendationContent.setText("AI가 전체 공간에 대한 맞춤 루틴을 생성 중입니다...\n잠시만 기다려 주세요. 🤔");
        currentRecommendations.clear();

        aiRoutineApiService.generateAiRoutineRecommendations(request).enqueue(new Callback<List<Recommendation>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recommendation>> call, @NonNull Response<List<Recommendation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Recommendation> recommendations = response.body();
                    currentRecommendations.addAll(recommendations);
                    if (recommendations.isEmpty()) {
                        Log.d(TAG, "AI 전체 추천: 생성된 추천이 없습니다.");
                        tvRecommendationContent.setText("생성된 추천 루틴이 없습니다. 😥");
                    } else {
                        Log.d(TAG, "AI 전체 추천: " + recommendations.size() + "개 생성 성공!");
                        displayRecommendationsAsText(recommendations);
                        Toast.makeText(RoutineAllActivity.this, recommendations.size() + "개의 AI 추천을 받았습니다!", Toast.LENGTH_LONG).show();
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
            // 타입 정보는 표시하지 않음
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
     * AI 추천 내용을 바탕으로 사용자가 반복 주기 등을 설정하고 실제 루틴으로 저장하는 UI를 띄웁니다.
     */
    private void showRoutineApplyOptionsDialog(Recommendation recommendationToApply) {
        Log.d(TAG, "루틴 반영 옵션 표시 시도: " + recommendationToApply.getTitle());

        // AlertDialog에 커스텀 레이아웃(dialog_apply_routine_options.xml)을 inflate 합니다.
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_apply_routine_options, null); // ❗️이 레이아웃 파일 생성 필요

        final Spinner spinnerSpace = dialogView.findViewById(R.id.spinnerSpaceForRoutineDialog);
        final Spinner spinnerRepeatUnit = dialogView.findViewById(R.id.spinnerRepeatUnitDialog);
        final EditText etRepeatIntervalDialog = dialogView.findViewById(R.id.etRepeatIntervalDialog);
        // final Button btnSelectFirstDueDate = dialogView.findViewById(R.id.btnSelectFirstDueDateDialog);
        // final TextView tvSelectedFirstDueDate = dialogView.findViewById(R.id.tvSelectedFirstDueDateDialog);

        // 1. 공간 선택 Spinner 설정
        List<String> spaceNameListForSpinner = new ArrayList<>();
        final List<Integer> spaceIdListForSpinner = new ArrayList<>();
        int defaultSpaceSelectionIndex = 0;

        if (userSpaceList != null && !userSpaceList.isEmpty()) {
            for (int i = 0; i < userSpaceList.size(); i++) {
                Space space = userSpaceList.get(i);
                spaceNameListForSpinner.add(space.getName());
                spaceIdListForSpinner.add(space.getSpace_id());
                if (recommendationToApply.getSpace_id() != null && recommendationToApply.getSpace_id() == space.getSpace_id()) {
                    defaultSpaceSelectionIndex = i; // AI 추천에 space_id가 있다면 기본 선택
                }
            }
        } else {
            spaceNameListForSpinner.add("선택할 공간 없음"); // 공간이 없을 경우
            spaceIdListForSpinner.add(-1); // 유효하지 않은 ID
        }
        ArrayAdapter<String> spaceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spaceNameListForSpinner);
        spaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpace.setAdapter(spaceAdapter);
        if (!userSpaceList.isEmpty()) {
            spinnerSpace.setSelection(defaultSpaceSelectionIndex);
        }


        // 2. 반복 단위 Spinner 설정
        String[] repeatUnitsDisplay = {"반복 안함", "매일", "매주", "매월", "매년"};
        final String[] repeatUnitsApi = {"NONE", "DAY", "WEEK", "MONTH", "YEAR"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, repeatUnitsDisplay);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeatUnit.setAdapter(unitAdapter);
        spinnerRepeatUnit.setSelection(0); // 기본 "반복 안함"

        // 3. 반복 간격 EditText (기본값 1)
        etRepeatIntervalDialog.setText("1");

        // 4. AlertDialog 생성 및 표시
        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("'" + recommendationToApply.getTitle() + "' 루틴 상세 설정")
                .setPositiveButton("저장", (dialog, which) -> {
                    CleaningRoutine routineToSave = new CleaningRoutine();
                    routineToSave.setUser_id(currentUserId);
                    routineToSave.setTitle(recommendationToApply.getTitle());
                    routineToSave.setDescription(recommendationToApply.getDescription());

                    // 선택된 공간 ID 설정
                    int selectedSpacePosition = spinnerSpace.getSelectedItemPosition();
                    if (selectedSpacePosition >= 0 && selectedSpacePosition < spaceIdListForSpinner.size() && spaceIdListForSpinner.get(selectedSpacePosition) > 0) {
                        routineToSave.setSpace_id(spaceIdListForSpinner.get(selectedSpacePosition));
                    } else {
                        Toast.makeText(this, "유효한 공간을 선택해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 선택된 반복 단위 및 간격 설정
                    String selectedUnitApiValue = repeatUnitsApi[spinnerRepeatUnit.getSelectedItemPosition()];
                    routineToSave.setRepeat_unit(selectedUnitApiValue);

                    if (!selectedUnitApiValue.equals("NONE")) {
                        try {
                            int interval = Integer.parseInt(etRepeatIntervalDialog.getText().toString());
                            if (interval > 0) {
                                routineToSave.setRepeat_interval(interval);
                            } else {
                                Toast.makeText(this, "반복 간격은 1 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "유효한 반복 간격을 입력해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        routineToSave.setRepeat_interval(null);
                    }
                    // first_due_date는 백엔드에서 오늘 기준으로 자동 계산되도록 설정되어 있다고 가정
                    // 또는 여기서 DatePickerDialog로 입력받아 routineToSave.setFirst_due_date(...) 설정

                    saveRoutineToBackend(routineToSave);
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void saveRoutineToBackend(CleaningRoutine routineToSave) {
        if (routineApiService == null) { Toast.makeText(this, "네트워크 서비스 오류", Toast.LENGTH_SHORT).show(); return; }
        Log.d(TAG, "백엔드에 루틴 저장 요청: " + routineToSave.getTitle() + " for spaceId: " + routineToSave.getSpace_id());
        routineApiService.createRoutine(routineToSave).enqueue(new Callback<CleaningRoutine>() {
            @Override
            public void onResponse(@NonNull Call<CleaningRoutine> call, @NonNull Response<CleaningRoutine> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RoutineAllActivity.this, "'" + response.body().getTitle() + "' 루틴이 성공적으로 반영되었습니다!", Toast.LENGTH_LONG).show();
                    // 성공 후 처리: 예를 들어, 루틴 목록 화면으로 돌아가거나 현재 화면을 닫고 결과 전달
                    // setResult(RESULT_OK);
                    // finish();
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
            finish(); // 뒤로가기 버튼 클릭 시 현재 Activity 종료
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleApiError(Response<?> response, String defaultMessage) { /* 이전과 동일 */ }
    private void handleApiFailure(Throwable t, String defaultMessage) { /* 이전과 동일 */ }
}