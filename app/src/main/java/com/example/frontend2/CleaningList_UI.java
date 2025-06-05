package com.example.frontend2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.CleaningListAdapter;
import com.example.frontend2.api.ApiClient;
import com.example.frontend2.api.RoutineApi;
import com.example.frontend2.models.CleaningRoutine; // ❗️ CleaningRoutine 모델 사용

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CleaningList_UI extends AppCompatActivity implements CleaningListAdapter.OnCleaningEditListener {

    private static final String TAG = "CleaningList_UI";
    private Toolbar toolbar;
    private String spaceNameFromIntent;
    private int spaceIdFromIntent = -1;
    private int currentUserId = -1;

    private RecyclerView recyclerView;
    private CleaningListAdapter adapter;
    private List<CleaningRoutine> routineList = new ArrayList<>(); // CleaningRoutine 타입 사용
    private ImageView btnAddRoutine;

    private RoutineApi routineApiService;

    public static final String PREFS_NAME = "UserPrefs"; // Main_UI와 동일한 이름 사용
    public static final String KEY_USER_ID = "current_user_id"; // Main_UI와 동일한 키 사용

    private ActivityResultLauncher<Intent> addOrEditRoutineLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cleaning_list_ui);

        // Intent로부터 데이터 가져오기
        Intent intent = getIntent();
        if (intent != null) {
            spaceNameFromIntent = intent.getStringExtra("space_name");
            spaceIdFromIntent = intent.getIntExtra("space_id", -1); // Main_UI에서 전달한 space_id
            currentUserId = intent.getIntExtra("userId", -1); // Main_UI에서 전달한 userId
        }

        // userId를 Intent로 못 받았으면 SharedPreferences에서 시도 (백업)
        if (currentUserId == -1) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentUserId = prefs.getInt(KEY_USER_ID, -1);
        }

        if (currentUserId == -1) {
            Toast.makeText(this, "사용자 정보가 없습니다. 로그인 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "User ID not available in CleaningList_UI.");
            finish();
            return;
        }

        if (spaceIdFromIntent == -1) {
            Toast.makeText(this, "공간 정보가 없어 루틴을 표시할 수 없습니다.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Space ID not passed to CleaningList_UI or is invalid: " + spaceIdFromIntent);
            finish(); // 공간 ID 없으면 이 화면 의미 없음
            return;
        }
        Log.d(TAG, "Displaying routines for spaceId: " + spaceIdFromIntent + ", spaceName: " + spaceNameFromIntent + ", userId: " + currentUserId);

        toolbar = findViewById(R.id.toolbar_clist);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(spaceNameFromIntent != null ? spaceNameFromIntent + " 루틴 목록" : "청소 루틴");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.rv_clist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CleaningListAdapter(this, routineList, this); // CleaningRoutine 사용
        recyclerView.setAdapter(adapter);

        if (ApiClient.getClient() != null) {
            routineApiService = ApiClient.getClient().create(RoutineApi.class);
        } else {
            Log.e(TAG, "ApiClient.getClient() is null. Check Retrofit setup.");
            Toast.makeText(this, "네트워크 초기화 오류입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        addOrEditRoutineLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Log.d(TAG, "Returned from CleaningAdd_UI or AI_Screen with RESULT_OK. Refreshing routine list.");
                        fetchRoutinesForSpace(currentUserId, spaceIdFromIntent); // 목록 새로고침
                    } else {
                        Log.d(TAG, "Returned from CleaningAdd_UI or AI_Screen with result code: " + result.getResultCode());
                    }
                });

        btnAddRoutine = findViewById(R.id.im_cadd); // XML의 ID: im_cadd
        btnAddRoutine.setOnClickListener(v -> {
            Intent addIntent = new Intent(CleaningList_UI.this, CleaningAdd_UI.class);
            addIntent.putExtra("userId", currentUserId);
            addIntent.putExtra("spaceId", spaceIdFromIntent); // 현재 보고 있는 공간의 ID 전달
            // "mode"는 보내지 않으면 CleaningAdd_UI에서 일반 생성 모드로 인식
            addOrEditRoutineLauncher.launch(addIntent);
        });

        // 처음 화면 로드 시 루틴 목록 가져오기
        fetchRoutinesForSpace(currentUserId, spaceIdFromIntent);
    }

    // 화면이 다시 활성화될 때마다 목록을 새로고침할 수도 있음 (선택 사항)
    @Override
    protected void onResume() {
        super.onResume();
        // AI 추천 화면에서 루틴 반영 후 돌아왔을 때도 onActivityResult가 호출되므로,
        // 중복 호출을 피하거나, 필요하다면 여기서도 호출할 수 있음.
        // 여기서는 onActivityResult에서 새로고침하므로 주석 처리.
        // if (currentUserId != -1 && spaceIdFromIntent != -1 && routineApiService != null) {
        //     fetchRoutinesForSpace(currentUserId, spaceIdFromIntent);
        // }
    }


    private void fetchRoutinesForSpace(int userId, int spaceId) {
        Log.d(TAG, "루틴 목록 요청 API 호출: userId=" + userId + ", spaceId=" + spaceId);
        // TODO: 로딩 인디케이터 표시

        // RoutineApi에 getRoutinesByUserAndSpace(userId, spaceId)가 정의되어 있다고 가정
        Call<List<CleaningRoutine>> call = routineApiService.getRoutinesByUserAndSpace(userId, spaceId);

        call.enqueue(new Callback<List<CleaningRoutine>>() {
            @Override
            public void onResponse(@NonNull Call<List<CleaningRoutine>> call, @NonNull Response<List<CleaningRoutine>> response) {
                // TODO: 로딩 인디케이터 숨김
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body()); // 어댑터에 새 데이터 설정
                    Log.d(TAG, "루틴 목록 로드 성공: " + response.body().size() + "개");
                    if (response.body().isEmpty()) {
                        Toast.makeText(CleaningList_UI.this, "이 공간에 등록된 루틴이 없습니다. '+' 버튼으로 추가하세요.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    handleApiError(response, "루틴 목록 불러오기 실패");
                    adapter.setItems(new ArrayList<>()); // 실패 시 빈 목록으로
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CleaningRoutine>> call, @NonNull Throwable t) {
                // TODO: 로딩 인디케이터 숨김
                handleApiFailure(t, "루틴 목록 서버 연결 오류");
                adapter.setItems(new ArrayList<>()); // 실패 시 빈 목록으로
            }
        });
    }

    // CleaningListAdapter.OnCleaningEditListener 인터페이스 구현
    @Override
    public void onEditRequested(CleaningRoutine routineToEdit) {
        Log.d(TAG, "Adapter에서 루틴 수정 요청: " + routineToEdit.getTitle());
        Intent editIntent = new Intent(this, CleaningAdd_UI.class);
        editIntent.putExtra("mode", "edit");
        editIntent.putExtra("userId", currentUserId);
        editIntent.putExtra("spaceId", routineToEdit.getSpace_id()); // 수정할 루틴의 공간 ID
        editIntent.putExtra("routineIdToEdit", routineToEdit.getRoutine_id());
        editIntent.putExtra("currentTitle", routineToEdit.getTitle());
        editIntent.putExtra("currentDescription", routineToEdit.getDescription());
        editIntent.putExtra("currentRepeatUnit", routineToEdit.getRepeat_unit());
        if (routineToEdit.getRepeat_interval() != null) {
            editIntent.putExtra("currentRepeatInterval", routineToEdit.getRepeat_interval());
        }
        addOrEditRoutineLauncher.launch(editIntent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

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